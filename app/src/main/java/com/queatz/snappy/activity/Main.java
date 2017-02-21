package com.queatz.snappy.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.MainAdapter;
import com.queatz.snappy.adapter.MainTabAdapter;
import com.queatz.snappy.fragment.MapSlide;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Buy;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.ActionBar;
import com.queatz.snappy.ui.OnBackPressed;
import com.queatz.snappy.ui.SlideScreen;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/19/14.
 */
public class Main extends Activity {
    public Team team;

    private ActionBar mActionBar;
    private SlideScreen mSlideScreen;
    private boolean mDestroyed = false;

    private Object mContextObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;
        team.auth.setActivity(this);

        Log.d(Config.LOG_TAG, "auth = " + team.auth.getAuthParam());

        setContentView(R.layout.main);

        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.showImg();
        mActionBar.setAdapter(new MainTabAdapter(this));
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

        team.buy.pullPerson();

        mSlideScreen = (SlideScreen) findViewById(R.id.main_content);
        mSlideScreen.setAdapter(new MainAdapter(getFragmentManager()));

        mSlideScreen.setOnSlideCallback(new SlideScreen.OnSlideCallback() {
            @Override
            public void onSlide(int currentSlide, float offset) {
                mActionBar.setSlide(offset);
            }

            @Override
            public void onSlideChange(int slide) {
                mActionBar.selectPage(slide);
            }
        });

        mActionBar.setOnPageChangeListener(new ActionBar.OnPageChangeListener() {
            @Override
            public void onPageChange(int page) {
                mSlideScreen.setSlide(page);
            }
        });

        if(getIntent() != null) {
            onNewIntent(getIntent());
        }
        else {
            mActionBar.resolve();
        }

        team.advertise.enable(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String show = intent.getStringExtra("show");

        if(show != null) {
            mActionBar.setPage(
                    "parties".equals(show) ? 0 :
                    "map".equals(show) ? 1 :
                    "messages".equals(show) ? 2 : 0
            );
        }
        else  {
            mActionBar.resolve();
        }


        String mapFocusId = intent.getStringExtra("mapFocusId");

        if (mapFocusId != null) {
            DynamicRealmObject mapFocus = team.realm.where("Thing")
                    .equalTo(Thing.ID, mapFocusId)
                    .findFirst();

            ((MapSlide) mSlideScreen.getSlideFragment(1)).setMapFocus(mapFocus);
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
        if (team.camera.isOpen()) {
            team.camera.close();
        } else {
            Fragment slide = mSlideScreen.getSlideFragment(mSlideScreen.getSlide());
            if (!(slide instanceof OnBackPressed) || !((OnBackPressed) slide).onBackPressed()) {
                super.onBackPressed();
            }
        }
    }
}
