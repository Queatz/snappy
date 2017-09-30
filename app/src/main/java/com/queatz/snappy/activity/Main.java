package com.queatz.snappy.activity;

import android.app.Fragment;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.MainAdapter;
import com.queatz.snappy.fragment.ChatSlide;
import com.queatz.snappy.fragment.MapSlide;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Buy;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.OnBackPressed;
import com.queatz.snappy.ui.ZoomableImageView;
import com.queatz.snappy.ui.slidescreen.SlideScreen;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/19/14.
 */
public class Main extends FullscreenActivity {

    public static final String CHAT_SLIDE = "chat";
    public static final String MAP_SLIDE = "map";
    public static final String MESSAGES_SLIDE = "messages";
    public static final String APPS_SLIDE = "apps";

    public Team team;

    private SlideScreen mSlideScreen;
    private boolean mDestroyed = false;

    private Object mContextObject;
    private boolean isHome;
    private PreferenceManager.OnActivityResultListener onWallpaperChangedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;
        team.auth.setActivity(this);

        Log.d(Config.LOG_TAG, "auth = " + team.auth.getAuthParam());

        setContentView(R.layout.main);

       team.buy.callback(new Buy.PurchaseCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                if (Build.VERSION.SDK_INT >= 17 && Main.this.isDestroyed())
                    return;

                if (Main.this.mDestroyed)
                    return;

                team.buy.pullGoogle(Main.this);
            }
        });

        mSlideScreen = (SlideScreen) findViewById(R.id.main_content);
        mSlideScreen.setAdapter(new MainAdapter(getFragmentManager()));
        mSlideScreen.setOnSlideCallback(new SlideScreen.OnSlideCallback() {
            @Override
            public void onSlide(int currentSlide, float offsetPercentage) {

            }

            @Override
            public void onSlideChange(int currentSlide) {
                team.preferences.edit()
                        .putInt(Config.PREFERENCE_RECENT_MAIN_SCREEN, currentSlide)
                        .apply();

                switch (currentSlide) {
                    case 0:
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

                        team.view.setTop("main.chat");

                        break;
                    default:
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                        team.view.clearTop("main.chat");

                        break;
                }
            }
        });

        if(getIntent() != null) {
            onNewIntent(getIntent());
        }

        team.advertise.enable(this);

        wallpaper();

        onWallpaperChangedListener = new PreferenceManager.OnActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wallpaper();
                    }
                });

                return true;
            }
        };

        team.callbacks.set(Config.REQUEST_CODE_WALLPAPER_CHANGED, onWallpaperChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        team.callbacks.unset(Config.REQUEST_CODE_WALLPAPER_CHANGED, onWallpaperChangedListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String show = intent.getStringExtra(Config.EXTRA_SHOW);

        if(show != null) {
            mSlideScreen.setSlide(
                    CHAT_SLIDE.equals(show) ? 0 :
                    MAP_SLIDE.equals(show) ? 1 :
                    MESSAGES_SLIDE.equals(show) ? 2 :
                    APPS_SLIDE.equals(show) ? 2 : 0
            );
        } else {
            mSlideScreen.setSlide(team.preferences.getInt(Config.PREFERENCE_RECENT_MAIN_SCREEN, 0));
        }

        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            isHome = true;
            team.stack.closeAllExcept(getClass());
        }

        if (mSlideScreen.isExpose()) {
            mSlideScreen.expose(false);
        }

        String mapFocusId = intent.getStringExtra(Config.EXTRA_MAP_FOCUS_ID);

        if (mapFocusId != null) {
            DynamicRealmObject mapFocus = team.realm.where("Thing")
                    .equalTo(Thing.ID, mapFocusId)
                    .findFirst();

            ((MapSlide) mSlideScreen.getSlideFragment(MainAdapter.MAP_SLIDE)).setMapFocus(mapFocus);
        }

        String chatTopic = intent.getStringExtra(Config.EXTRA_CHAT_TOPIC);

        if (chatTopic != null) {
            ((ChatSlide) mSlideScreen.getSlideFragment(MainAdapter.CHAT_SLIDE)).setTopic(chatTopic);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        team.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        team.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (menu.hasVisibleItems()) {
            return;
        }

        mContextObject = view.getTag();
        team.menu.make(mContextObject, menu, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return team.menu.choose(this, mContextObject, item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
    }

    @Override
    public void onBackPressed() {
        if (ZoomableImageView.isZooming()) {
            ZoomableImageView.close();
        } else if (team.camera.isOpen()) {
            team.camera.close();
        } else {
            Fragment slide = mSlideScreen.getSlideFragment(mSlideScreen.getSlide());
            if (!(slide instanceof OnBackPressed) || !((OnBackPressed) slide).onBackPressed()) {
                if (!mSlideScreen.isExpose()) {
                    mSlideScreen.expose(true);
                    return;
                }

                // Cannot go back from the phone's home screen
                if (isHome) {
                    return;
                }

                super.onBackPressed();
            }
        }
    }

    private void wallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!wallpaperManager.isWallpaperSupported()) {
                return;
            }
        }

        Drawable drawable = wallpaperManager.getDrawable();

        if (drawable == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                drawable = wallpaperManager.getBuiltInDrawable();
            }
        }

        if (drawable == null) {
            return;
        }

        mSlideScreen.setBackground(drawable);
    }
}
