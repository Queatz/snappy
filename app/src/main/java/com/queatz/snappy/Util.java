package com.queatz.snappy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.TimeUtil;

import java.io.IOException;
import java.util.UUID;

import io.realm.DynamicRealmObject;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by jacob on 10/31/14.
 */
public class Util {
    public static Context context;
    public static Team team;

    public static String getDistanceText(double distance) {
        if (distance < 1) {
            int ft = ((int) distance * 5280);

            if (ft < 250) {
                return context.getString(R.string.right_here);
            } else if (ft <= 1000) {
                ft = (ft / 250) * 250;
            } else {
                ft = (ft / 500) * 500;
            }

            return context.getResources().getQuantityString(R.plurals.num_feet, ft, ft);
        } else {
            int mi = (int) distance;
            return context.getResources().getQuantityString(R.plurals.num_miles, mi, mi);
        }
    }

    public static CharSequence fancyFormat(int resId, Object... params) {
        return Html.fromHtml(String.format(context.getString(resId), params));
    }

    public static CharSequence fancyName(DynamicRealmObject person) {
        final SpannableStringBuilder stringBuilder = new SpannableStringBuilder(Functions.getFullName(person));
        final ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.spacer));

        int start = person.getString(Thing.FIRST_NAME).length() + 1;
        stringBuilder.setSpan(colorSpan, start, start + person.getString(Thing.LAST_NAME).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

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

    public static String offerAmount(@NonNull final DynamicRealmObject offer) {
        if (offer.isNull(Thing.PRICE)) {
            return team.context.getString(R.string.ask);
        } else {
            return "$" + Math.abs(offer.getInt(Thing.PRICE)) +
                    (offer.isNull(Thing.UNIT) ||
                            offer.getString(Thing.UNIT) == null ||
                            offer.getString(Thing.UNIT).isEmpty() ? "" :
                            "/" + offer.getString(Thing.UNIT));
        }
    }

    public static boolean offerIsRequest(@NonNull final DynamicRealmObject offer) {
        return !offer.isNull(Thing.PRICE) && offer.getInt(Thing.PRICE) < 0;
    }

    public static String offerPriceText(@NonNull final DynamicRealmObject offer, boolean isShorthand) {
        if (!isShorthand) {
            return offerPriceText(offer);
        }

        return offer.isNull(Thing.PRICE) ? context.getString(R.string.ask) : offer.getInt(Thing.PRICE) > 0
                ? Util.offerAmount(offer) : offer.getInt(Thing.PRICE) < 0 ? "+" + Util.offerAmount(offer)
                : context.getString(R.string.free);
    }

    public static String offerPriceText(@NonNull final DynamicRealmObject offer) {
        return offer.isNull(Thing.PRICE) ? context.getString(R.string.ask) : offer.getInt(Thing.PRICE) > 0 ?
                context.getString(R.string.for_amount, Util.offerAmount(offer)) : offer.getInt(Thing.PRICE) < 0 ?
                context.getString(R.string.make_amount, Util.offerAmount(offer)) :
                context.getString(R.string.for_free);
    }

    public static String offerMessagePrefill(@NonNull final DynamicRealmObject offer) {
        return context.getString(offerIsRequest(offer) ? R.string.ive_got_offer : R.string.id_like_offer, offer.getString(Thing.ABOUT));
    }

    public static Spanned getUpdateText(DynamicRealmObject update) {
        boolean past;
        Spanned string;

        switch (update.getString(Thing.ACTION)) {
            case Config.UPDATE_ACTION_HOST_PARTY:
                if(update.getObject(Thing.TARGET) == null || update.getObject(Thing.PERSON) == null)
                    return null;

                past = TimeUtil.isPartyPast(update.getObject(Thing.TARGET));

                string = Html.fromHtml(String.format(context.getString(past ? R.string.update_text_hosted : R.string.update_text_is_hosting), update.getObject(Thing.PERSON).getString(Thing.FIRST_NAME), update.getObject(Thing.TARGET).getString(Thing.NAME), TimeUtil.agoDate(update.getObject(Thing.TARGET).getDate(Thing.DATE))));
                break;
            case Config.UPDATE_ACTION_JOIN_PARTY:
                past = TimeUtil.isPartyPast(update.getObject(Thing.TARGET));

                string = Html.fromHtml(String.format(context.getString(past ? R.string.update_text_went_to : R.string.update_text_is_going_to), update.getObject(Thing.PERSON).getString(Thing.FIRST_NAME), update.getObject(Thing.TARGET).getString(Thing.NAME), TimeUtil.agoDate(update.getObject(Thing.TARGET).getDate(Thing.DATE))));
                break;
            case Config.UPDATE_ACTION_UPTO:
            default:
                string = new SpannableString(update.getString(Thing.ABOUT));
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

    public static String locationPhoto(DynamicRealmObject location, int s) {
        return Config.API_URL + String.format(Config.PATH_EARTH_PHOTO + "?s=" + s + "&auth=" + team.auth.getAuthParam(), location.getString(Thing.ID));
    }

    public static String photoUrl(String path, int s) {
        return Config.API_URL + path + "?s=" + s + "&auth=" + team.auth.getAuthParam();
    }

    public static boolean liked(@NonNull DynamicRealmObject update, @NonNull DynamicRealmObject person) {
        return team.realm.where("Thing").equalTo("source.id", person.getString(Thing.ID)).equalTo("target.id", update.getString(Thing.ID)).count() != 0;
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

    public static void attachFAB(final FloatingActionButton floatingAction, final ListView list) {
        list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_MOVE ||
                        event.getHistorySize() < 1 ||
                        event.getHistoricalY(0) == event.getY()) {
                    return false;
                }

                if (event.getY() > event.getHistoricalY(0)) {
                    floatingAction.show();
                } else {
                    floatingAction.hide();
                }

                return false;
            }
        });
    }
}
