package com.queatz.snappy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Update;
import com.queatz.snappy.util.TimeUtil;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by jacob on 10/31/14.
 */
public class Util {
    public static Context context;
    public static Team team;

    public static CharSequence fancyName(Person person) {
        final SpannableStringBuilder stringBuilder = new SpannableStringBuilder(person.getName());
        final ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.spacer));

        int start = person.getFirstName().length() + 1;
        stringBuilder.setSpan(colorSpan, start, start + person.getLastName().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return stringBuilder;
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

    public static Spanned getUpdateText(Update update) {
        boolean past;
        Spanned string = null;

        switch (update.getAction()) {
            case Config.UPDATE_ACTION_HOST_PARTY:
                if(update.getParty() == null || update.getPerson() == null)
                    return null;

                past = TimeUtil.isPartyPast(update.getParty());

                string = Html.fromHtml(String.format(context.getString(past ? R.string.update_text_hosted : R.string.update_text_is_hosting), update.getPerson().getFirstName(), update.getParty().getName(), TimeUtil.agoDate(update.getParty().getDate())));
                break;
            case Config.UPDATE_ACTION_JOIN_PARTY:
                past = TimeUtil.isPartyPast(update.getParty());

                string = Html.fromHtml(String.format(context.getString(past ? R.string.update_text_went_to : R.string.update_text_is_going_to), update.getPerson().getFirstName(), update.getParty().getName(), TimeUtil.agoDate(update.getParty().getDate())));
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
