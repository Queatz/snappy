package com.queatz.snappy;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

/**
 * Created by jacob on 10/31/14.
 */
public class Util {
    public static float px(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float dp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static String cuteDate(Context context, Date date) {
        Calendar now = GregorianCalendar.getInstance();
        Calendar party = GregorianCalendar.getInstance();
        party.setTime(date);
        party.setTimeZone(SimpleTimeZone.getDefault());

        int day;

        if(now.get(Calendar.DAY_OF_YEAR) == party.get(Calendar.DAY_OF_YEAR)) {
            if (party.get(Calendar.HOUR_OF_DAY) > 19)
                day = R.string.tonight;
            else
                day = R.string.today;
        }
        else if(now.get(Calendar.DAY_OF_YEAR) + 1 == party.get(Calendar.DAY_OF_YEAR)) {
            day = R.string.tomorrow;
        }
        else {
            return "-";
        }

        String time = party.get(Calendar.HOUR) + (party.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm");

        return String.format(context.getResources().getString(day), time);
    }
}
