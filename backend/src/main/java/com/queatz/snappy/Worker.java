package com.queatz.snappy;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.RegistrationRecord;
import com.queatz.snappy.service.Search;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.queatz.snappy.backend.OfyService.ofy;

/**
 * Created by jacob on 4/11/15.
 */
public class Worker extends HttpServlet {
    private static final String API_KEY = System.getProperty("gcm.api.key");

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

                Sender sender = new Sender(API_KEY);
                Message msg = new Message.Builder().addData("message", message).build();

                HashSet<SendInstance> toUsers = new HashSet<>();

                // Specific

                if(toUser != null) {
                    toUsers.add(new SendInstance(toUser, Config.SOCIAL_MODE_OFF));
                }

                // Social Mode: Friends

                if(fromUser != null) {
                    Results<ScoredDocument> results = Search.getService().index.get(Search.Type.FOLLOW).search("following = \"" + fromUser + "\"");

                    if(results != null) {
                        for(Document follow : results) {
                            toUsers.add(new SendInstance(follow.getOnlyField("person").getAtom(), Config.SOCIAL_MODE_FRIENDS));
                        }
                    }
                }

                // Social Mode: On
                // Friends will have higher priority due to HashSet not overwriting existing elements

                if(fromUser != null) {
                    GeoPoint fromLocation = Search.getService().get(Search.Type.PERSON, fromUser).getOnlyField("latlng").getGeoPoint();

                    if(fromLocation != null) {
                        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PERSON).search("distance(latlng, geopoint(" + fromLocation.getLatitude() + ", " + fromLocation.getLongitude() + ")) < " + Config.SEARCH_DISTANCE);

                        if (results != null) {
                            for (Document person : results) {
                                if(fromUser.equals(person.getId()))
                                    continue;

                                toUsers.add(new SendInstance(person.getId(), Config.SOCIAL_MODE_ON));
                            }
                        }
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
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
}
