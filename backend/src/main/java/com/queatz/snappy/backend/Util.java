package com.queatz.snappy.backend;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by jacob on 2/16/15.
 */
public class Util {
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    public static String genToken() {
        return UUID.randomUUID().toString() +
                UUID.randomUUID().toString() +
                UUID.randomUUID().toString();
    }

    static public Date longToDate(long millis) {
        return new Date(millis);
    }

    static DateFormat formatter = DateFormat.getDateTimeInstance(
            DateFormat.LONG,
            DateFormat.LONG,
            Locale.US);

    public static Date stringToDate(String date) {
        try {
            return formatter.parse(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateToString(Date date) {
        return formatter.format(date);
    }

    // Helper function until Datastore has geo-spacial query support
    public static String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper function until Datastore has geo-spacial query support
    public static String decode(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String googleUrl(String url) throws MalformedURLException {
        return new URL(url).getPath().substring(2);
    }
}
