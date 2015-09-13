package com.queatz.snappy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;

import com.luckycatlabs.sunrisesunset.Zenith;
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Update;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by jacob on 10/31/14.
 */
public class Util {
    public static Context context;
    public static Team team;

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

    public static void setupWithContext(Context ctx) {
        context = ctx;
    }

    public static String createLocalId() {
        return "local:" + UUID.randomUUID().toString();
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

    public static String relDate(Date date) {
        return agoDate(date);
    }

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
        return cuteDate(date, false);
    }

    public static String cuteDate(Date date, boolean inSentence) {
        if(context == null || date == null)
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

        return String.format(context.getResources().getString(day), time);
    }

    public static boolean isPartyPast(Party party) {
        return party.getDate().before(new Date(new Date().getTime() - 1000 * 60 * 60));
    }

    public static Spanned getUpdateText(Update update) {
        boolean past;
        Spanned string = null;

        switch (update.getAction()) {
            case Config.UPDATE_ACTION_HOST_PARTY:
                if(update.getParty() == null || update.getPerson() == null)
                    return null;

                past = isPartyPast(update.getParty());

                string = Html.fromHtml(String.format(context.getString(past ? R.string.update_text_hosted : R.string.update_text_is_hosting), update.getPerson().getFirstName(), update.getParty().getName(), agoDate(update.getParty().getDate())));
                break;
            case Config.UPDATE_ACTION_JOIN_PARTY:
                past = isPartyPast(update.getParty());

                string = Html.fromHtml(String.format(context.getString(past ? R.string.update_text_went_to : R.string.update_text_is_going_to), update.getPerson().getFirstName(), update.getParty().getName(), agoDate(update.getParty().getDate())));
                break;
            case Config.UPDATE_ACTION_UPTO:
            default:
                string = new SpannableString(update.getMessage());
                break;
        }

        return string;
    }

    public static String nextSocialMode(String socialMode) {
        if(socialMode == null) {
            return Config.SOCIAL_MODE_OFF;
        }

        switch (socialMode) {
            case Config.SOCIAL_MODE_OFF:
                return Config.SOCIAL_MODE_FRIENDS;
            case Config.SOCIAL_MODE_FRIENDS:
                return Config.SOCIAL_MODE_ON;
            case Config.SOCIAL_MODE_ON:
            default:
                return Config.SOCIAL_MODE_OFF;
        }
    }

    public static String locationPhoto(com.queatz.snappy.things.Location location, int s) {
        return Config.API_URL + String.format(Config.PATH_LOCATION_PHOTO + "?s=" + s + "&auth=" + team.auth.getAuthParam(), location.getId());
    }

    public static Matrix transformationFromExif(Uri uri) {
        try {
            int orientation = new ExifInterface(uri.getPath()).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            if(orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                return null; // Picasso will handle it
            }

            orientation = getOrientationFromCursor(uri);

            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.preRotate(orientation);
                return matrix;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    static int getOrientationFromCursor(Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        cursor.moveToFirst();

        int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }

    static int getExifRotation(int orientation) {
        int rotation;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
            case ExifInterface.ORIENTATION_TRANSPOSE:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                rotation = 270;
                break;
            default:
                rotation = 0;
        }
        return rotation;
    }

    static int getExifTranslation(int orientation)  {
        int translation;
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                translation = -1;
                break;
            default:
                translation = 1;
        }
        return translation;
    }
}
