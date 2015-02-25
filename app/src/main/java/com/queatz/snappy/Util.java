package com.queatz.snappy;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.luckycatlabs.sunrisesunset.Zenith;
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator;
import com.queatz.snappy.team.Team;

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
    public static Team team;

    public static void setupWithContext(Context ctx) {
        context = ctx;
    }

    public static float px(float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float dp(float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    private static HashMap<Date, Boolean> cachedDaytimes = new HashMap<>();

    public static boolean isDaytime(Date date) {
        if(team == null)
            return false;

        if(cachedDaytimes.containsKey(date))
            return cachedDaytimes.get(date);

        Location location = team.location.get();

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

    public static boolean everybodyIsSleeping(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int hour = cal.get(Calendar.HOUR_OF_DAY);

        return hour < 6 || hour > 22;
    }

    public static Date matchDateHour(Date date) {
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, calDate.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if(cal.getTime().before(new Date()))
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);

        return cal.getTime();
    }

    public static Date quantizeDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long WEEK_MILLIS = 7 * DAY_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    private static final long YEAR_MILLIS = 356 * DAY_MILLIS;

    public static String agoDate(Date date) {
        long timeDiff = new Date().getTime() - date.getTime();

        boolean future = timeDiff < 0;

        if(future)
            timeDiff *= -1;

        long c;
        String s;

        if(timeDiff > YEAR_MILLIS) {
            c = timeDiff / YEAR_MILLIS;
            s = String.format(context.getResources().getQuantityString(R.plurals.years, (int) c), c);
        }
        else if(timeDiff > MONTH_MILLIS) {
            c = timeDiff / MONTH_MILLIS;
            s = String.format(context.getResources().getQuantityString(R.plurals.months, (int) c), c);
        }
        else if(timeDiff > WEEK_MILLIS) {
            c = timeDiff / WEEK_MILLIS;
            s = String.format(context.getResources().getQuantityString(R.plurals.weeks, (int) c), c);
        }
        else if(timeDiff > DAY_MILLIS) {
            c = timeDiff / DAY_MILLIS;
            s = String.format(context.getResources().getQuantityString(R.plurals.days, (int) c), c);
        }
        else if(timeDiff > HOUR_MILLIS) {
            c = timeDiff / HOUR_MILLIS;
            s = String.format(context.getResources().getQuantityString(R.plurals.hours, (int) c), c);
        }
        else if(timeDiff > MINUTE_MILLIS) {
            c = timeDiff / MINUTE_MILLIS;
            s = String.format(context.getResources().getQuantityString(R.plurals.minutes, (int) c), c);
        }
        else {
            return "just now";
        }

        return String.format(context.getString(future ? R.string.time_in : R.string.time_ago), s);
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
