package com.queatz.snappy.activity;

import android.app.Activity;
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
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Buy;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.ActionBar;
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
        mActionBar.setRightContent(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                team.action.openProfile(Main.this, team.auth.me());
            }
        });

        mActionBar.setRightContent("profile menu");

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

        ImageView profile = ((ImageView) mActionBar.getRightContent().getChildAt(0));

        if(profile != null) {
            String usr = team.auth.getUser();

            if(usr != null) {
                DynamicRealmObject person = team.things.get(usr);

                if(person != null) {
                    Picasso.with(this)
                            .load(Functions.getImageUrlForSize(person, (int) Util.px(64)))
                            .placeholder(R.color.spacer)
                            .into(profile);
                }
            }
        }

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

                getWindow().setSoftInputMode(slide == 1 ? WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE: WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

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
            mActionBar.setPage("parties".equals(show) ? 0 : "messages".equals(show) ? 1 : 0);
        }
        else  {
            mActionBar.resolve();
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
}
