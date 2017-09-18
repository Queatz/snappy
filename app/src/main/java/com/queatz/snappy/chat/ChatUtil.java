package com.queatz.snappy.chat;

import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatUtil {
    public static String defaultTopicImg(String topic) {
        return Config.VILLAGE_WEBSITE + "img/topics/" + topic.toLowerCase().replace(" ", "%20") + ".png";
    }

    public static String defaultAvatarImg(String avatar) {
        return Config.VILLAGE_WEBSITE + "img/avatars/" + avatar.toLowerCase() + ".png";
    }
}
