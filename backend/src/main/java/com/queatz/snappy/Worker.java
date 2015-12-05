package com.queatz.snappy;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.RegistrationRecord;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.FollowLinkSpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.queatz.snappy.backend.Datastore.ofy;

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String action = req.getParameter("action");

        switch (action) {
            case "message":
                String toUser = req.getParameter("toUser");
                String fromUser = req.getParameter("fromUser");
                String message = req.getParameter("message");

                Sender sender = new Sender(Config.GCM_KEY);
                Message msg = new Message.Builder().addData("message", message).build();

                HashSet<SendInstance> toUsers = new HashSet<>();

                // Specific

                if(toUser != null) {
                    toUsers.add(new SendInstance(toUser, Config.SOCIAL_MODE_OFF));
                }

                // Social Mode: Friends

                if(fromUser != null) {
                    for(FollowLinkSpec follow : Datastore.get(FollowLinkSpec.class).filter("targetId", Datastore.key(PersonSpec.class, fromUser))) {
                        toUsers.add(new SendInstance(Datastore.id(follow.sourceId), Config.SOCIAL_MODE_FRIENDS));
                    }
                }

                // Social Mode: On
                // Friends will have higher priority due to HashSet not overwriting existing elements

                if(fromUser != null) {
                    PersonSpec source = Datastore.get(PersonSpec.class, fromUser);
                    for (PersonSpec person : Search.getService().getNearby(PersonSpec.class, source.latlng, new Date(new Date().getTime() - 1000 * 60 * 60 * Config.MAX_IDLE_HOURS), 300)) {
                        if(fromUser.equals(person.id))
                            continue;

                        toUsers.add(new SendInstance(person.id, Config.SOCIAL_MODE_ON));
                    }
                }

                // Send

                for(SendInstance sendInstance : toUsers) {
                    List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).filter("userId", sendInstance.userId).list();
                    for (RegistrationRecord record : records) {
                        if(sendInstance.lowestRequiredSocialMode != null && record.getSocialMode() != null) {
                            if(
                                    (
                                            Config.SOCIAL_MODE_OFF.equals(record.getSocialMode()) &&
                                            (sendInstance.lowestRequiredSocialMode.equals(Config.SOCIAL_MODE_FRIENDS) || sendInstance.lowestRequiredSocialMode.equals(Config.SOCIAL_MODE_ON))
                                    ) ||
                                    (
                                            Config.SOCIAL_MODE_FRIENDS.equals(record.getSocialMode()) &&
                                            (sendInstance.lowestRequiredSocialMode.equals(Config.SOCIAL_MODE_ON))
                                    )
                            ) {
                                continue;
                            }
                        }

                        try {
                            Result result = sender.send(msg, record.getRegId(), 5);

                            if (result.getMessageId() != null) {
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
                            Logger.getLogger("push").warning("error sending" + e);
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
}
