package com.queatz.snappy;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.googlecode.objectify.ObjectifyService;
import com.queatz.snappy.backend.RegistrationRecord;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthEmail;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthRef;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by jacob on 4/11/15.
 */
public class Worker extends HttpServlet {

    static {
        ObjectifyService.register(RegistrationRecord.class);
    }

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String action = req.getParameter("action");

        String toUser = req.getParameter("toUser");
        String fromUser = req.getParameter("fromUser");
        String data = req.getParameter("message");

        EarthAs as = new EarthAs();

        Eventable eventable = new EarthUpdate(as).from(action, data);

        HashSet<SendInstance> toUsers = new HashSet<>();

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

            EarthGeo latLng;

            if (source.has(EarthField.GEO)) {
                latLng = source.getGeo(EarthField.GEO);
            } else {
                latLng = earthStore.get(source.getKey(EarthField.SOURCE)).getGeo(EarthField.GEO);
            }

            for (EarthThing person : new EarthStore(as).getNearby(latLng, EarthKind.PERSON_KIND, null)) {
                if(fromUser.equals(person.key().name())) {
                    continue;
                }

                toUsers.add(new SendInstance(person.key().name(), Config.SOCIAL_MODE_ON));
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

        for(SendInstance sendInstance : toUsers) {
            List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).filter("userId", sendInstance.userId).list();

            if (push == null || records.isEmpty()) {
                if (!passesSocialMode(sendInstance, records.isEmpty() ? Config.SOCIAL_MODE_FRIENDS : pickHighestSocialMode(records))) {
                    continue;
                }

                sendEmail(as, eventable, sendInstance.userId);
                continue;
            }

            boolean didSendPush = false;

            // Send to devices
            for (RegistrationRecord record : records) {
                if (!passesSocialMode(sendInstance, record.getSocialMode())) {
                    continue;
                }

                // XXX TODO RETRIES
                JsonObject results = send(push, record.getRegId());

                if (results.has("results") && results.getAsJsonArray("results").size() > 0) {
                    JsonObject result = results.getAsJsonArray("results").get(0).getAsJsonObject();

                    if (result.has("registration_id")) {
                        String canonicalRegId = result.get("registration_id").getAsString();
                        record.setRegId(canonicalRegId);
                        ofy().save().entity(record).now();
                    }

                    if (result.has("error")) {
                        if ("MismatchSenderId".equals(result.get("error").getAsString()) ||
                                "NotRegistered".equals(result.get("error").getAsString())) {
                            ofy().delete().entity(record).now();
                        }
                    } else {
                        didSendPush = true;
                    }
                }
            }

            if (!didSendPush) {
                sendEmail(as, eventable, sendInstance.userId);
            }
        }
    }

    private String pickHighestSocialMode(List<RegistrationRecord> records) {
        String socialMode = null;

        for (RegistrationRecord record : records) {
            if (socialMode == null || compareSocialModes(record.getSocialMode(), socialMode)) {
                socialMode = record.getSocialMode();
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
            URL url = new URL(Config.FCM_ENDPOINT);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.POST);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            httpRequest.setPayload(earthJson.toJson(push).getBytes("UTF-8"));
            httpRequest.addHeader(new HTTPHeader("Content-Type", "application/json; charset=UTF-8"));
            httpRequest.addHeader(new HTTPHeader("Authorization", "key=" + Config.GCM_KEY));
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            String s = new String(resp.getContent(), "UTF-8");
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

    private void sendEmail(EarthAs as, Eventable eventable, String toUser) {
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
}
