package com.queatz.snappy.shared;

import com.queatz.snappy.shared.earth.EarthGeo;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by jacob on 9/17/17.
 */

public class Shared {
    public static DateFormat formatter = DateFormat.getDateTimeInstance(
            DateFormat.LONG,
            DateFormat.LONG,
            Locale.US);

    public static String randomToken() {
        Random random = new Random();
        return Long.toString(random.nextLong()) +
                Long.toString(random.nextLong()) +
                Long.toString(random.nextLong());
    }

    public static double distance(double lat1, double long1, double lat2, double long2) {
        double d2r = (Math.PI / 180.0);
        double dlong = (long2 - long1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = pow(sin(dlat/2.0), 2) + cos(lat1*d2r) * cos(lat2*d2r) * pow(sin(dlong/2.0), 2);
        double c = 2 * atan2(sqrt(a), sqrt(1-a));
        return 3956 * c; // miles
    }

    public static double quantizedDistance(double distance) {
        return Math.floor(distance / Config.personLocationAccuracy) * Config.personLocationAccuracy;
    }

    public static double distance(EarthGeo a, EarthGeo b) {
        return quantizedDistance(distance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude()));
    }

    public static String genToken() {
        return UUID.randomUUID().toString() +
                UUID.randomUUID().toString() +
                UUID.randomUUID().toString();
    }

    public static Date stringToDate(String date) {
        try {
            return formatter.parse(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public Date longToDate(long millis) {
        return new Date(millis);
    }

    public static String dateToString(Date date) {
        return formatter.format(date);
    }

    public static String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    public static String clip(String string) {
        return string.length() > 100 ? string.substring(0, 100) : string;
    }
}
