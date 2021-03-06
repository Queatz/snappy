package com.queatz.snappy;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.OnScrollActions;
import com.queatz.snappy.team.ScrollActionsTouchListener;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;
import com.queatz.snappy.ui.camera.CameraImageSaver;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;
import com.queatz.snappy.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;

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

    public static double distance(double lat1, double long1, double lat2, double long2) {
        double d2r = (Math.PI / 180.0);
        double dlong = (long2 - long1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = pow(sin(dlat/2.0), 2) + cos(lat1*d2r) * cos(lat2*d2r) * pow(sin(dlong/2.0), 2);
        double c = 2 * atan2(sqrt(a), sqrt(1-a));
        return 3956 * c; // miles
    }

    public static String getDistanceText(double distance) {
        if (distance < 1) {
            int ft = ((int) (distance * 5280f));

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
        return (!offer.isNull(Thing.WANT) && offer.getBoolean(Thing.WANT)) ||
                (!offer.isNull(Thing.PRICE) && offer.getInt(Thing.PRICE) < 0);
    }

    public static String offerPriceText(@NonNull final DynamicRealmObject offer) {
        return offer.isNull(Thing.PRICE) ? context.getString(R.string.interested) : offer.getInt(Thing.PRICE) > 0 ?
                context.getString(R.string.for_amount, Util.offerAmount(offer)) : offer.getInt(Thing.PRICE) < 0 ?
                context.getString(R.string.for_amount, Util.offerAmount(offer)) :
                context.getString(R.string.interested);
    }

    public static String offerMessagePrefill(@NonNull final DynamicRealmObject offer) {
        if (offer.isNull(Thing.PRICE) || offer.getInt(Thing.PRICE) == 0) {
            return context.getString(R.string.interested_for, offer.getString(Thing.ABOUT));
        }

        return context.getString(offerIsRequest(offer) ? R.string.ive_got_offer : R.string.id_like_offer, offer.getString(Thing.ABOUT));
    }

    public static Spanned getUpdateText(DynamicRealmObject update) {
        boolean past, recentlyPast;
        Spanned string;

        String action = update.getString(Thing.ACTION);

        if (action == null) {
            return new SpannableString(!update.isNull(Thing.ABOUT) ? update.getString(Thing.ABOUT) : "");
        }

        switch (action) {
            case Config.UPDATE_ACTION_HOST_PARTY:
                if(update.getObject(Thing.TARGET) == null || update.getObject(Thing.SOURCE) == null)
                    return null;

                past = TimeUtil.isPartyPast(update.getObject(Thing.TARGET));
                recentlyPast = TimeUtil.isPartyRecentlyPast(update.getObject(Thing.TARGET));

                string = Html.fromHtml(String.format(context.getString(recentlyPast ? R.string.update_text_is_hosting_as_of : past ? R.string.update_text_hosted : R.string.update_text_is_hosting), update.getObject(Thing.TARGET).getString(Thing.NAME), TimeUtil.agoDate(update.getObject(Thing.TARGET).getDate(Thing.DATE))));
                break;
            case Config.UPDATE_ACTION_JOIN_PARTY:
                past = TimeUtil.isPartyPast(update.getObject(Thing.TARGET));
                recentlyPast = TimeUtil.isPartyRecentlyPast(update.getObject(Thing.TARGET));

                string = Html.fromHtml(String.format(context.getString(recentlyPast ? R.string.update_text_is_going_to_as_of : past ? R.string.update_text_went_to : R.string.update_text_is_going_to), update.getObject(Thing.TARGET).getString(Thing.NAME), TimeUtil.agoDate(update.getObject(Thing.TARGET).getDate(Thing.DATE))));
                break;
            case Config.UPDATE_ACTION_UPTO:
            default:
                string = new SpannableString(!update.isNull(Thing.ABOUT) ? update.getString(Thing.ABOUT) : "");
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
        return Config.API_URL + String.format(Config.PATH_EARTH_PHOTO + "?s=" + (s > 0 ? s : 64) + "&auth=" + team.auth.getAuthParam(), location.getString(Thing.ID));
    }

    public static String photoUrl(String path, int s) {
        return Config.API_URL + path + "?s=" + s + "&auth=" + team.auth.getAuthParam();
    }

    public static boolean liked(@NonNull DynamicRealmObject update, @NonNull DynamicRealmObject person) {
        return team.realm.where("Thing").equalTo("source.id", person.getString(Thing.ID)).equalTo("target.id", update.getString(Thing.ID)).count() != 0;
    }

    public static boolean isModeOn(@NonNull DynamicRealmObject mode, @Nullable DynamicRealmObject person) {
        if (person == null) {
            return false;
        }

        DynamicRealmObject member = team.realm.where("Thing")
                .equalTo(Thing.KIND, ThingKinds.MEMBER)
                .equalTo("target.id", person.getString(Thing.ID))
                .equalTo("source.id", mode.getString(Thing.ID))
                .findFirst();

        return member != null;
    }

    @Nullable
    public static DynamicRealmObject getModeMember(@Nullable DynamicRealmObject mode, @Nullable DynamicRealmObject person) {
        if (person == null || mode == null) {
            return null;
        }

        return team.realm.where("Thing")
                .equalTo(Thing.KIND, ThingKinds.MEMBER)
                .equalTo("target.id", person.getString(Thing.ID))
                .equalTo("source.id", mode.getString(Thing.ID))
                .findFirst();
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

    public static ScrollActionsTouchListener setOnScrollActions(View view, OnScrollActions actions) {
        ScrollActionsTouchListener listener = new ScrollActionsTouchListener(actions);
        view.setOnTouchListener(listener);
        return listener;
    }

    public static void setPhotoWithPicasso(DynamicRealmObject thing, int s, ImageView photo) {
        photo.setImageDrawable(null);
        Images.with(context).cancelRequest(photo);

        String photoUrl = Util.photoUrl(Config.PATH_EARTH + "/" + thing.getString(Thing.ID) + "/" + Config.PATH_PHOTO, s / 2);

        if (thing.isNull(Thing.PLACEHOLDER)) {
            Images.with(context)
                    .load(photoUrl)
                    .placeholder(R.color.spacer)
                    .into(photo);
        } else {
            Images.with(context)
                    .load(photoUrl)
                    .placeholder(placeholder(context.getResources(), thing.getString(Thing.PLACEHOLDER)))
                    .into(photo);

        }

        if (!thing.isNull(Thing.ASPECT)) {
            final float aspect = (float) thing.getDouble(Thing.ASPECT);
            photo.getLayoutParams().height = (int) (s / aspect);
        }
    }

    private static BitmapDrawable placeholder(Resources resources, String image) {
        byte bytes[] = Base64.decode(image, Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return new BitmapDrawable(resources, bitmap);
    }

    public static Bitmap tint(int color) {
        int s = (int) px(16);
        Paint paint = new Paint();
        paint.setColor(color);
        Bitmap bitmapResult = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawOval(0, 0, s, s, paint);
        return bitmapResult;
    }

    public static Uri uriFromImage(Image image) {
        File file = new File(context.getExternalFilesDir(null), "village-capture-" + new Date() + ".jpg");
        new CameraImageSaver(image, file).run();
        return Uri.fromFile(file);
    }

    public static String getProximityText(DynamicRealmObject person) {
        return getDistanceText(person.getDouble(Thing.INFO_DISTANCE)) + (
                person.isNull(Thing.INFO_UPDATED) ? "" : " " +
                        TimeUtil.agoDate(person.getDate(Thing.INFO_UPDATED))
        );
    }

    private static final long GRACE_DELAY = 2000; // Toast.LENGTH_SHORT

    public static void autoHideWindowUI(final Window window) {
        final Runnable onSystemUiVisibilityChangeListener = new Runnable() {
            @Override
            public void run() {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        };

        final View.OnSystemUiVisibilityChangeListener onUiChanged = new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                window.getDecorView().removeCallbacks(onSystemUiVisibilityChangeListener);
                window.getDecorView().postDelayed(onSystemUiVisibilityChangeListener, GRACE_DELAY);
            }
        };

        onUiChanged.onSystemUiVisibilityChange(window.getDecorView().getSystemUiVisibility());
        window.getDecorView().setOnSystemUiVisibilityChangeListener(onUiChanged);
    }

    /**
     * Creates a member. Does not manage its own transaction.
     * @param source The source, or "member"
     * @param target The target, or "membership provider"
     * @return The newly created member
     */
    public static DynamicRealmObject createMember(DynamicRealmObject source, DynamicRealmObject target) {
        DynamicRealmObject member = team.realm.createObject("Thing");
        member.setString(Thing.KIND, ThingKinds.MEMBER);
        member.setObject(Thing.SOURCE, source);
        member.setObject(Thing.TARGET, target);
        member.setString(Thing.STATUS, Config.MEMBER_STATUS_ACTIVE);
        return member;
    }

    public static RealmResults<DynamicRealmObject> membersOf(DynamicRealmObject thing, String membersOfKind) {
        return thing.getList(Thing.MEMBERS)
                .where()
                .equalTo(Thing.SOURCE + "." + Thing.KIND, membersOfKind)
                .findAll();
    }

    /**
     * Maps a list of object to a certain object field, dropping them if that field does not exist.
     * @param list The list to map
     * @return The mapped list
     */
    public static List<DynamicRealmObject> mapToObjectField(RealmResults<DynamicRealmObject> list, String field) {
        ArrayList<DynamicRealmObject> results = new ArrayList<>();

        for (DynamicRealmObject thing : list) {
            if (thing.hasField(field)) {
                results.add(thing.getObject(field));
            }
        }

        return results;
    }
}
