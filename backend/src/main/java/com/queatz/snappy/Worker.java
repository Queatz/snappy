package com.queatz.snappy;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.queatz.snappy.email.EmailOptions;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.email.EarthEmail;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.shared.earth.EarthRef;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.events.Eventable;
import com.queatz.snappy.logic.editors.DeviceEditor;
import com.queatz.snappy.logic.mines.DeviceMine;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.util.HttpUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 4/11/15.
 */
public class Worker extends HttpServlet {

    private static class SendInstance {
        protected String userId;
        protected String lowestRequiredSocialMode;

        protected SendInstance(String userId, String lowestRequiredSocialMode) {
            this.userId = userId;
            this.lowestRequiredSocialMode = lowestRequiredSocialMode;
        }

        @Override
        public int hashCode() {
            return this.userId.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof SendInstance && ((SendInstance) other).userId.equals(this.userId);
        }
    }

    private static class GeoSubscribeInstance {
        protected String email;
        protected String locality;
        protected String token;

        protected GeoSubscribeInstance(String email, String locality, String token) {
            this.email = email;
            this.locality = locality;
            this.token = token;
        }

        @Override
        public int hashCode() {
            return this.token.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof GeoSubscribeInstance && ((GeoSubscribeInstance) other).token.equals(this.token);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String action = req.getParameter("action");

        String toUser = req.getParameter("toUser");
        String fromUser = req.getParameter("fromUser");
        String location = req.getParameter("location");
        String data = req.getParameter("message");
        EarthGeo latLng = null;

        EarthAs as = new EarthAs();

        Eventable eventable = new EarthUpdate(as).from(action, data);

        HashSet<Object> toUsers = new HashSet<>();

        // Specific

        if(toUser != null) {
            toUsers.add(new SendInstance(toUser, Config.SOCIAL_MODE_OFF));
        }

        // Social Mode: Friends

        if(fromUser != null) {
            final EarthStore earthStore = new EarthStore(as);

            for(EarthThing follow : earthStore.find(EarthKind.FOLLOWER_KIND, EarthField.TARGET, EarthRef.of(fromUser))) {
                toUsers.add(new SendInstance(follow.getKey(EarthField.SOURCE).name(), Config.SOCIAL_MODE_FRIENDS));
            }

            // Social Mode: On
            // Friends will have higher priority due to HashSet not overwriting existing elements

            EarthThing source = earthStore.get(fromUser);

            if (source != null) {
                // Use own geo, or source geo
                if (source.has(EarthField.GEO)) {
                    latLng = source.getGeo(EarthField.GEO);
                } else {
                    latLng = earthStore.get(source.getKey(EarthField.SOURCE)).getGeo(EarthField.GEO);
                }
            }
        }

        if (location != null) {
            latLng = new EarthJson().fromJson(location, EarthGeo.class);
        }

        if (latLng != null) {
            for (EarthThing thing : new EarthStore(as).getNearby(latLng, EarthKind.PERSON_KIND + "|" + EarthKind.GEO_SUBSCRIBE_KIND, null)) {
                if (fromUser != null && fromUser.equals(thing.key().name())) {
                    continue;
                }

                if (EarthKind.PERSON_KIND.equals(thing.getString(EarthField.KIND))) {
                    toUsers.add(new SendInstance(thing.key().name(), Config.SOCIAL_MODE_ON));
                } else if (EarthKind.GEO_SUBSCRIBE_KIND.equals(thing.getString(EarthField.KIND))) {
                    toUsers.add(new GeoSubscribeInstance(
                            thing.getString(EarthField.EMAIL),
                            thing.getString(EarthField.NAME),
                            thing.getString(EarthField.UNSUBSCRIBE_TOKEN)
                    ));
                }
            }
        }

        // Send

        Object pushObject = eventable.makePush();

        final JsonObject push;

        if (pushObject != null) {
            push = new JsonObject();

            JsonObject message = new JsonObject();
            message.add("message", new EarthJson().toJsonTree(pushObject));
            push.add("data", message);
            push.add("priority", new JsonPrimitive("high"));
        } else {
            push = null;
        }

        for(Object sendInstance : toUsers) {
            if (sendInstance instanceof SendInstance) {
                handlePersonSendInstance((SendInstance) sendInstance, push, as, eventable);
            } else if (sendInstance instanceof GeoSubscribeInstance) {
                handleGeoSubscribeInstance((GeoSubscribeInstance) sendInstance, eventable);
            }
        }
    }

    private void handleGeoSubscribeInstance(GeoSubscribeInstance sendInstance, Eventable eventable) {
        EmailOptions options = new EmailOptions()
                .setSubject(eventable.makeSubject())
                .setBody(eventable.makeEmail())
                .setFooter("<br /><br /><span style=\"color: #757575;\">You're subscribed to " + sendInstance.locality + ".  To unsubscribe, click <a href=\"" + Config.API_URL + Config.PATH_EARTH + "/" + Config.PATH_GEO_SUBSCRIBE + "?unsubscribe=" + sendInstance.token + "\">here</a>.</span>");
        sendToEmail(options, sendInstance.email);
    }

    private void handlePersonSendInstance(SendInstance sendInstance, JsonObject push, EarthAs as, Eventable eventable) {
        List<EarthThing> devices = new DeviceMine(as).forUser(sendInstance.userId);

        if (push == null || devices.isEmpty()) {
            if (!passesSocialMode(sendInstance, devices.isEmpty() ? Config.SOCIAL_MODE_FRIENDS : pickHighestSocialMode(devices))) {
                return;
            }

            sendEmailToPerson(as, eventable, sendInstance.userId);
            return;
        }

        boolean didSendPush = false;

        // Send to devices
        for (EarthThing device : devices) {
            if (!passesSocialMode(sendInstance, device.getString("socialMode"))) {
                continue;
            }

            // XXX TODO RETRIES
            JsonObject results = send(push, device.getString("regId"));

            if (results.has("results") && results.getAsJsonArray("results").size() > 0) {
                JsonObject result = results.getAsJsonArray("results").get(0).getAsJsonObject();

                if (result.has("registration_id")) {
                    String canonicalRegId = result.get("registration_id").getAsString();
                    new DeviceEditor(as).setRegId(device, canonicalRegId);
                }

                if (result.has("error")) {
                    if ("MismatchSenderId".equals(result.get("error").getAsString()) ||
                            "NotRegistered".equals(result.get("error").getAsString())) {
                        new DeviceEditor(as).remove(device);
                    }
                } else {
                    didSendPush = true;
                }
            }
        }

        if (!didSendPush) {
            sendEmailToPerson(as, eventable, sendInstance.userId);
        }
    }

    private String pickHighestSocialMode(List<EarthThing> devices) {
        String socialMode = null;

        for (EarthThing device : devices) {
            if (socialMode == null || compareSocialModes(device.getString("socialMode"), socialMode)) {
                socialMode = device.getString("socialMode");
            }
        }

        return socialMode;
    }

    private static Map<String, Integer> socialModeValue = ImmutableMap.of(
            Config.SOCIAL_MODE_OFF, 0,
            Config.SOCIAL_MODE_FRIENDS, 1,
            Config.SOCIAL_MODE_ON, 2
    );

    private boolean compareSocialModes(String socialMode, String isGreaterThanSocialMode) {
        return socialModeValue.get(socialMode) > socialModeValue.get(isGreaterThanSocialMode);
    }

    private JsonObject send(JsonObject push, String regId) {
        EarthJson earthJson = new EarthJson();
        push = (JsonObject) earthJson.toJsonTree(push);

        // Add device reg id to payload
        push.add("to", new JsonPrimitive(regId));

        try {
            String s = HttpUtil.post(Config.FCM_ENDPOINT, "application/json; charset=UTF-8", earthJson.toJson(push).getBytes("UTF-8"), "key=" + Config.GCM_KEY);
            Logger.getLogger(Config.NAME).log(Level.INFO, s);
            return earthJson.fromJson(s, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean passesSocialMode(SendInstance sendInstance, String socialMode) {
        if(sendInstance.lowestRequiredSocialMode != null && socialMode != null) {
            if(
                    (
                            Config.SOCIAL_MODE_OFF.equals(socialMode) &&
                                    (sendInstance.lowestRequiredSocialMode.equals(Config.SOCIAL_MODE_FRIENDS) || sendInstance.lowestRequiredSocialMode.equals(Config.SOCIAL_MODE_ON))
                    ) ||
                            (
                                    Config.SOCIAL_MODE_FRIENDS.equals(socialMode) &&
                                            (sendInstance.lowestRequiredSocialMode.equals(Config.SOCIAL_MODE_ON))
                            )
                    ) {
                return false;
            }
        }

        return true;
    }

    private void sendEmailToPerson(EarthAs as, Eventable eventable, String toUser) {
        final String subject = eventable.makeSubject();

        // Not an email-able notification
        if (subject == null) {
            return;
        }

        new EarthEmail().sendRawEmail(
                new EarthStore(as).get(toUser).getString(EarthField.EMAIL),
                subject,
                eventable.makeEmail());
    }

    private void sendToEmail(EmailOptions options, String email) {
        // Not an email-able notification
        if (options.getSubject() == null) {
            return;
        }

        new EarthEmail().sendRawEmail(
                email,
                options.getSubject(),
                options.getCompleteEmail());
    }
}
