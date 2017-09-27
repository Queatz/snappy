package com.queatz.snappy.logic;

/**
 * Created by jacob on 9/27/17.
 */

public class WebsiteHelper {
    private static String[] topLevelUrls = new String[] {
            "messages",
            "hubs",
            "clubs",
            "projects",
            "resources",
            "people",
            "chat",
            "forms",
            "search",
            "settings",
            "authenticate",
    };

    public static boolean isReservedUrl(String string) {
        for (String url : topLevelUrls) {
            if (url.equals(string)) {
                return true;
            }
        }

        return false;
    }
}
