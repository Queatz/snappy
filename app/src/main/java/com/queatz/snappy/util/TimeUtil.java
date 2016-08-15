package com.queatz.snappy.util;

import android.location.Location;

import com.luckycatlabs.sunrisesunset.Zenith;
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Thing;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/14/15.
 */
public class TimeUtil {
    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long YEAR_MILLIS = 365 * DAY_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    private static final long WEEK_MILLIS = 7 * DAY_MILLIS;
    private static final DateFormat formatter = DateFormat.getDateTimeInstance(
            DateFormat.LONG,
            DateFormat.LONG,
            Locale.US);

    private static HashMap<Date, Boolean> cachedDaytimes = new HashMap<>();

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

    public static boolean isDaytime(Date date) {
        if(Util.team == null)
            return false;

        if(cachedDaytimes.containsKey(date))
            return cachedDaytimes.get(date);

        Location location = Util.team.location.get();

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

    public static String relDate(Date date) {
        return agoDate(date);
    }

    public static String agoDate(Date date) {
        return agoDate(date, true);
    }

    public static String agoDate(Date date, boolean includeText) {
        long timeDiff = new Date().getTime() - date.getTime();

        boolean future = timeDiff < 0;

        if(future)
            timeDiff *= -1;

        long c;
        String s;

        if(timeDiff > YEAR_MILLIS) {
            c = timeDiff / YEAR_MILLIS;
            s = String.format(Util.context.getResources().getQuantityString(R.plurals.years, (int) c), c);
        }
        else if(timeDiff > MONTH_MILLIS) {
            c = timeDiff / MONTH_MILLIS;
            s = String.format(Util.context.getResources().getQuantityString(R.plurals.months, (int) c), c);
        }
        else if(timeDiff > WEEK_MILLIS) {
            c = timeDiff / WEEK_MILLIS;
            s = String.format(Util.context.getResources().getQuantityString(R.plurals.weeks, (int) c), c);
        }
        else if(timeDiff > DAY_MILLIS) {
            c = timeDiff / DAY_MILLIS;
            s = String.format(Util.context.getResources().getQuantityString(R.plurals.days, (int) c), c);
        }
        else if(timeDiff > HOUR_MILLIS) {
            c = timeDiff / HOUR_MILLIS;
            s = String.format(Util.context.getResources().getQuantityString(R.plurals.hours, (int) c), c);
        }
        else if(timeDiff > MINUTE_MILLIS) {
            c = timeDiff / MINUTE_MILLIS;
            s = String.format(Util.context.getResources().getQuantityString(R.plurals.minutes, (int) c), c);
        }
        else {
            return "just now";
        }

        if (!includeText) {
            return s;
        }

        return String.format(Util.context.getString(future ? R.string.time_in : R.string.time_ago), s);
    }

    public static String cuteDate(Date date) {
        return cuteDate(date, false);
    }

    public static String cuteDate(Date date, boolean inSentence) {
        if(Util.context == null || date == null)
            return "-";

        Calendar now = GregorianCalendar.getInstance();
        Calendar party = GregorianCalendar.getInstance();
        party.setTime(date);
        party.setTimeZone(SimpleTimeZone.getDefault());

        int day;

        if(now.get(Calendar.DAY_OF_YEAR) == party.get(Calendar.DAY_OF_YEAR)) {
            if (isDaytime(date))
                day = inSentence ? R.string.sentence_today : R.string.today;
            else
                day = inSentence ? R.string.sentence_tonight : R.string.tonight;
        }
        else if(now.get(Calendar.DAY_OF_YEAR) + 1 == party.get(Calendar.DAY_OF_YEAR)) {
            day = inSentence ? R.string.sentence_tomorrow : R.string.tomorrow;
        }
        else {
            return "-";
        }

        int hour = party.get(Calendar.HOUR);
        if(hour == 0) hour = 12;

        String time = hour + (party.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm");

        return String.format(Util.context.getResources().getString(day), time);
    }

    public static boolean isPartyPast(DynamicRealmObject party) {
        return party.getDate(Thing.DATE).before(new Date(new Date().getTime() - 1000 * 60 * 60));
    }
}
