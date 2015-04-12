package com.queatz.snappy.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.MainAdapter;
import com.queatz.snappy.adapter.MainTabAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.ui.ActionBar;
import com.queatz.snappy.ui.SlideScreen;
import com.squareup.picasso.Picasso;

/**
 * Created by jacob on 10/19/14.
 */
public class Main extends Activity {
    public Team team;

    private ActionBar mActionBar;
    private SlideScreen mSlideScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;
        team.auth.setActivity(this);

        Log.d(Config.LOG_TAG, "auth = " + team.auth.getAuthParam());

        setContentView(R.layout.main);

        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setAdapter(new MainTabAdapter(this));
        mActionBar.setRightContent(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getApplication()).team;

                team.action.openMinimenu(Main.this, view);
            }
        });

        if(!team.buy.bought()) {
            team.buy.pullPerson();
            team.buy.pullGoogle(this);
        }

        ImageView profile = ((ImageView) mActionBar.getRightContent().getChildAt(0));

        if(profile != null) {
            String usr = team.auth.getUser();

            if(usr != null) {
                Person person = team.things.get(Person.class, usr);

                if(person != null) {
                    Picasso.with(this).load(person.getImageUrlForSize((int) Util.px(64))).placeholder(R.color.spacer).into(profile);
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
            mActionBar.setPage(0);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Boolean showPostHostMessage = intent.getBooleanExtra("show_post_host_message", false);
        String show = intent.getStringExtra("show");

        mActionBar.setPage(show == null || "parties".equals(show) ? 0 : "messages".equals(show) ? 1 : 0);

        if(showPostHostMessage) {
            if(!team.preferences.getBoolean(Config.PREFERENCE_HOST_PARTY_SCREEN_SHOWN, false)) {
                new AlertDialog.Builder(this)
                        .setMessage(team.context.getString(R.string.message_host_party))
                        .setPositiveButton(team.context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                team.preferences.edit().putBoolean(Config.PREFERENCE_HOST_PARTY_SCREEN_SHOWN, true).apply();
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        team.buy.onActivityResult(this, requestCode, resultCode, data);
    }
}
