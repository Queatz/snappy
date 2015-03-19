package com.queatz.snappy.service;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.queatz.snappy.SnappyServlet;
import com.queatz.snappy.backend.RegistrationRecord;

import java.io.IOException;
import java.util.List;

import static com.queatz.snappy.backend.OfyService.ofy;

/**
 * Created by jacob on 3/18/15.
 */
public class Push {
    public SnappyServlet snappy;

    private static final String API_KEY = System.getProperty("gcm.api.key");

    public Push(SnappyServlet s) {
        snappy = s;
    }

    public void register(String user, String device) {
        RegistrationRecord record = findRecord(device);

        if (record != null) {
            if(user.equals(record.getUserId()))
                return;
        }
        else {
            record = new RegistrationRecord();
        }

        record.setRegId(device);
        record.setUserId(user);
        ofy().save().entity(record).now();
    }

    public void unregister(String user, String device) {
        RegistrationRecord record = findRecord(device);
        if (record == null) {
            return;
        }
        ofy().delete().entity(record).now();
    }

    public void send(final String user, final String toUser, final String message) {
        try {
            sendMessage(user, toUser, message);
        }
        catch (IOException e) {
            e.printStackTrace();
            // Yikes, user will never get the push notification
            // Use a redis queue
        }
    }

    public void clear(String messageId) {
        // Sends a push to all devices with this message to clear it (user has handled it)
    }

    private void sendMessage(String user, String toUser, String message) throws IOException {
        if (message == null || message.trim().length() == 0) {
            return;
        }
        // crop longer messages
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "...";
        }
        Sender sender = new Sender(API_KEY);
        Message msg = new Message.Builder().addData("message", message).build();
        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).filter("userId", toUser).list();
        for (RegistrationRecord record : records) {
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
                    // if the device is no longer registered with Gcm, remove it from the datastore
                    ofy().delete().entity(record).now();
                }
            }
        }
    }

    private RegistrationRecord findRecord(String regId) {
        return ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
    }
}
