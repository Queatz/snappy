package com.queatz.snappy.events;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.shared.Config;

import java.util.List;

/**
 * A bunch of unused functions...
 *
 * Created by jacob on 2/16/15.
 */
public class Util {

    public static String findHighestSocialMode(List<EarthThing> devices) {
        String socialMode = Config.SOCIAL_MODE_OFF;

        for (EarthThing device : devices) {
            if (Config.SOCIAL_MODE_ON.equals(device.getString("socialMode"))) {
                return Config.SOCIAL_MODE_ON;
            } else if (Config.SOCIAL_MODE_FRIENDS.equals(device.getString("socialMode"))) {
                socialMode = Config.SOCIAL_MODE_FRIENDS;
            }
        }

        return socialMode;
    }
}
