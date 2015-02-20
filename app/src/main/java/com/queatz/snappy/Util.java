package com.queatz.snappy;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.luckycatlabs.sunrisesunset.Zenith;
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Created by jacob on 10/31/14.
 */
public class Util {
    public static Context context;

    public static void setupWithContext(Context ctx) {
        context = ctx;
    }

    public static float px(float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float dp(float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static Location getLatLong() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        LocationManager locationManager = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));

        return locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
    }

    private static HashMap<Date, Boolean> cachedDaytimes = new HashMap<>();

    public static boolean isDaytime(Date date) {
        if(cachedDaytimes.containsKey(date))
            return cachedDaytimes.get(date);

        Location location = getLatLong();

        if(location == null)
            return true;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SolarEventCalculator solarEventCalculator = new SolarEventCalculator(new com.luckycatlabs.sunrisesunset.dto.Location(location.getLatitude(), location.getLongitude()), TimeZone.getDefault());
        Date sunrise = solarEventCalculator.computeSunriseCalendar(Zenith.CIVIL, cal).getTime();
        Date sunset = solarEventCalculator.computeSunsetCalendar(Zenith.CIVIL, cal).getTime();

        boolean is = date.after(sunrise) && date.before(sunset);

        cachedDaytimes.put(date, is);

        return is;
    }

    public static String cuteDate(Date date) {
        if(context == null || date == null)
            return "-";

        Calendar now = GregorianCalendar.getInstance();
        Calendar party = GregorianCalendar.getInstance();
        party.setTime(date);
        party.setTimeZone(SimpleTimeZone.getDefault());

        int day;

        if(now.get(Calendar.DAY_OF_YEAR) == party.get(Calendar.DAY_OF_YEAR)) {
            if (isDaytime(date))
                day = R.string.today;
            else
                day = R.string.tonight;
        }
        else if(now.get(Calendar.DAY_OF_YEAR) + 1 == party.get(Calendar.DAY_OF_YEAR)) {
            day = R.string.tomorrow;
        }
        else {
            return "-";
        }

        int hour = party.get(Calendar.HOUR);
        if(hour == 0) hour = 12;

        String time = hour + (party.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm");

        return String.format(context.getResources().getString(day), time);
    }
}
