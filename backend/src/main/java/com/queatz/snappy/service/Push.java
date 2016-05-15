package com.queatz.snappy.service;

import com.googlecode.objectify.ObjectifyService;
import com.queatz.snappy.backend.RegistrationRecord;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.shared.PushSpec;

import java.util.Date;

import static com.googlecode.objectify.ObjectifyService.ofy;

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

    static {
        ObjectifyService.register(RegistrationRecord.class);
    }

    public Push() {

    }

    public void register(String user, String device, String socialMode) {
        RegistrationRecord record = findRecord(user, device);

        if (record != null) {
            record.setSocialMode(socialMode);
            record.setUpdated(new Date());
            ofy().save().entity(record).now();
        }
        else {
            record = new RegistrationRecord();
            record.setRegId(device);
            record.setUserId(user);
            record.setSocialMode(socialMode);
            record.setCreated(new Date());
            record.setUpdated(record.getCreated());
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

    public void send(final String toUser, final PushSpec message) {
        if(message == null)
            return;

        // XXX TODO Should use new MessagePush().toJson()
        Queue.getService().enqueuePushMessageToUser(toUser, EarthSingleton.of(EarthJson.class).toJson(message));
    }

    public void sendToFollowers(final String fromUser, final PushSpec message) {
        if(message == null)
            return;

        Queue.getService().enqueuePushMessageFromUser(fromUser, EarthSingleton.of(EarthJson.class).toJson(message));
    }

    public void clear(String messageId) {
        // Sends a push to all devices with this message to clear it (user has handled it)
    }

    private RegistrationRecord findRecord(String userId, String regId) {
        return ofy().load().type(RegistrationRecord.class).filter("regId", regId).filter("userId", userId).first().now();
    }
}
