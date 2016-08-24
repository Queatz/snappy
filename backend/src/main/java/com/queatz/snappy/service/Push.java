package com.queatz.snappy.service;

import com.googlecode.objectify.ObjectifyService;
import com.queatz.snappy.backend.RegistrationRecord;
import com.queatz.snappy.shared.Config;

import java.util.Date;
import java.util.List;

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

    public void clear(String messageId) {
        // Sends a push to all devices with this message to clear it (user has handled it)
    }

    private RegistrationRecord findRecord(String userId, String regId) {
        return ofy().load().type(RegistrationRecord.class).filter("regId", regId).filter("userId", userId).first().now();
    }

    public String getSocialMode(String userId) {
        return findHighestSocialMode(ofy().load().type(RegistrationRecord.class)
                .filter("userId", userId).list());
    }

    private String findHighestSocialMode(List<RegistrationRecord> devices) {
        String socialMode = Config.SOCIAL_MODE_OFF;

        for (RegistrationRecord device : devices) {
            if (Config.SOCIAL_MODE_ON.equals(device.getSocialMode())) {
                return Config.SOCIAL_MODE_ON;
            } else if (Config.SOCIAL_MODE_FRIENDS.equals(device.getSocialMode())) {
                socialMode = Config.SOCIAL_MODE_FRIENDS;
            }
        }

        return socialMode;
    }
}
