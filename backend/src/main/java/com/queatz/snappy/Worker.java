package com.queatz.snappy;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.googlecode.objectify.ObjectifyService;
import com.queatz.snappy.backend.RegistrationRecord;
import com.queatz.snappy.logic.EarthEmail;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSearcher;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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

        Eventable eventable = new EarthUpdate(null).from(action, data);

        HashSet<SendInstance> toUsers = new HashSet<>();

        // Specific

        if(toUser != null) {
            toUsers.add(new SendInstance(toUser, Config.SOCIAL_MODE_OFF));
        }

        // Social Mode: Friends

        if(fromUser != null) {
            final EarthStore earthStore = new EarthStore(null);

            for(Entity follow : earthStore.find(EarthKind.FOLLOWER_KIND, EarthField.TARGET, earthStore.key(fromUser))) {
                toUsers.add(new SendInstance(follow.getKey(EarthField.SOURCE).name(), Config.SOCIAL_MODE_FRIENDS));
            }

            // Social Mode: On
            // Friends will have higher priority due to HashSet not overwriting existing elements

            Entity source = earthStore.get(fromUser);

            LatLng latLng;

            if (source.contains(EarthField.GEO)) {
                latLng = source.getLatLng(EarthField.GEO);
            } else {
                latLng = earthStore.get(source.getKey(EarthField.SOURCE)).getLatLng(EarthField.GEO);
            }

            for (Entity person : new EarthSearcher(null).getNearby(EarthKind.PERSON_KIND, null, latLng, 300)) {
                if(fromUser.equals(person.key().name())) {
                    continue;
                }

                toUsers.add(new SendInstance(person.key().name(), Config.SOCIAL_MODE_ON));
            }
        }

        // Send

        final Sender sender = new Sender(Config.GCM_KEY);
        final Message msg = new Message.Builder().addData("message", new EarthJson().toJson(eventable.makePush())).build();

        for(SendInstance sendInstance : toUsers) {
            List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).filter("userId", sendInstance.userId).list();


            if (records.isEmpty()) {
//                        if (!passesSocialMode(sendInstance, Config.SOCIAL_MODE_FRIENDS)) {
//                            continue;
//                        }

                sendEmail(eventable, sendInstance.userId);
                continue;
            }

            boolean didSendPush = false;

            // Send to devices
            for (RegistrationRecord record : records) {
                if (!passesSocialMode(sendInstance, record.getSocialMode())) {
                    continue;
                }

                try {
                    Result result = sender.send(msg, record.getRegId(), 5);

                    if (result.getMessageId() != null) {
                        didSendPush = true;

                        String canonicalRegId = result.getCanonicalRegistrationId();
                        if (canonicalRegId != null) {
                            record.setRegId(canonicalRegId);
                            ofy().save().entity(record).now();
                        }
                    } else {
                        String error = result.getErrorCodeName();
                        if (error.equals(Constants.ERROR_NOT_REGISTERED) || error.equals(Constants.ERROR_MISMATCH_SENDER_ID)) {
                            ofy().delete().entity(record).now();
                        }
                    }
                } catch (IOException e) {
                    Logger.getLogger("push").warning("error sending, trying email fallback " + e);
                    e.printStackTrace();
                }
            }

            if (!didSendPush) {
                sendEmail(eventable, sendInstance.userId);
            }
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
                return true;
            }
        }

        return false;
    }

    private void sendEmail(Eventable eventable, String toUser) {
        final String subject = eventable.makeSubject();

        // Not an email-able notification
        if (subject == null) {
            return;
        }

        new EarthEmail().sendRawEmail(
                new EarthStore(null).get(toUser).getString(EarthField.EMAIL),
                subject,
                eventable.makeEmail());
    }
}
