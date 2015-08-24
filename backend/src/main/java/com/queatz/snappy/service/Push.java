package com.queatz.snappy.service;

import com.queatz.snappy.SnappyServlet;
import com.queatz.snappy.backend.RegistrationRecord;

import org.json.JSONObject;

import static com.queatz.snappy.backend.OfyService.ofy;

/**
 * Created by jacob on 3/18/15.
 */
public class Push {
    private static Push _service;

    public static Push getService() {
        if(_service == null)
            _service = new Push();

        return _service;
    }

    public Push() {
    }

    public void register(String user, String device, String socialMode) {
        RegistrationRecord record = findRecord(user, device);

        if (record != null) {
            record.setSocialMode(socialMode);
            ofy().save().entity(record).now();
        }
        else {
            record = new RegistrationRecord();
            record.setRegId(device);
            record.setUserId(user);
            record.setSocialMode(socialMode);
            ofy().save().entity(record).now();
        }
    }

    public void unregister(String user, String device) {
        RegistrationRecord record = findRecord(user, device);
        if (record == null) {
            return;
        }
        ofy().delete().entity(record).now();
    }

    public void send(final String toUser, final JSONObject message) {
        if(message == null)
            return;

        Queue.getService().enqueuePushMessageToUser(toUser, message.toString());
    }

    public void sendToFollowers(final String fromUser, final JSONObject message) {
        if(message == null)
            return;

        Queue.getService().enqueuePushMessageFromUser(fromUser, message.toString());
    }

    public void clear(String messageId) {
        // Sends a push to all devices with this message to clear it (user has handled it)
    }

    private RegistrationRecord findRecord(String userId, String regId) {
        return ofy().load().type(RegistrationRecord.class).filter("regId", regId).filter("userId", userId).first().now();
    }
}
