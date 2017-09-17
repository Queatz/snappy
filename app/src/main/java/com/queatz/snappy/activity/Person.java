package com.queatz.snappy.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PersonAdapter;
import com.queatz.snappy.adapter.ProfileAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.TeamActivity;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.SetSeenAction;
import com.queatz.snappy.team.contexts.PersonContext;
import com.queatz.snappy.ui.OnBackPressed;
import com.queatz.snappy.ui.slidescreen.SlideScreen;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/19/14.
 */
public class Person extends TeamActivity implements PersonContext {

    public static final int SLIDE_PROFILE = 0;
    public static final int SLIDE_MESSAGES = 1;

    private SlideScreen mSlideScreen;
    private DynamicRealmObject mPerson;
    private boolean mIsActive;

    private Object mContextObject;

    @Override
    public DynamicRealmObject getPerson() {
        return mPerson;
    }

    public SlideScreen getSlideScreen() {
        return mSlideScreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsActive = true;

        Intent intent = getIntent();

        if(intent == null) {
            Log.w(Config.LOG_TAG, "Null intent");
            return;
        }

        String id = intent.getStringExtra(Config.EXTRA_PERSON_ID);

        if(id == null) {
            Log.w(Config.LOG_TAG, "No person specified");
            return;
        }

        mPerson = getTeam().realm.where("Thing").equalTo(Thing.ID, id).findFirst();
        // Else load person and wait

        if(mPerson == null) {
            Log.w(Config.LOG_TAG, "Person with id wasn't found in local database");
            return;
        }

        setContentView(R.layout.person);

        boolean itsMe = getTeam().auth.getUser() != null && getTeam().auth.getUser().equals(mPerson.getString(Thing.ID));

        mSlideScreen = (SlideScreen) findViewById(R.id.person_content);

        if (itsMe) {
            mSlideScreen.setAdapter(new ProfileAdapter(getFragmentManager(), mPerson));
        } else {
            PersonAdapter personAdapter = new PersonAdapter(getFragmentManager(), mPerson);

            String message = intent.getStringExtra("message");
            personAdapter.setMessagePrefill(message);

            mSlideScreen.setAdapter(personAdapter);

        }

        mSlideScreen.setOnSlideCallback(new SlideScreen.OnSlideCallback() {
            @Override
            public void onSlide(int currentSlide, float offset) {
            }

            @Override
            public void onSlideChange(int slide) {
                if(mPerson != null) {
                    if (slide == SLIDE_MESSAGES && mIsActive) {
                        to(new SetSeenAction(mPerson));
                        getTeam().push.clear("messages");
                        getTeam().view.setTop("person/" + mPerson.getString(Thing.ID) + "/messages");
                    } else {
                        getTeam().view.clearTop("person/" + mPerson.getString(Thing.ID) + "/messages");
                    }
                }
            }
        });

        if(getIntent() != null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String show = intent.getStringExtra(Config.EXTRA_SHOW);

        if(show == null) {
            return;
        }

        mSlideScreen.setSlide("upto".equals(show) ? SLIDE_PROFILE : "messages".equals(show) ? SLIDE_MESSAGES : SLIDE_PROFILE);
    }

    @Override
    public void onResume() {
        super.onResume();

        mIsActive = true;

        if(mPerson != null && mSlideScreen.getSlide() == SLIDE_MESSAGES) {
            getTeam().view.setTop("person/" + mPerson.getString(Thing.ID) + "/messages");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mIsActive = false;

        if (mPerson != null) {
            getTeam().view.clearTop("person/" + mPerson.getString(Thing.ID) + "/messages");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getTeam().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getTeam().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (menu.hasVisibleItems()) {
            return;
        }

        mContextObject = view.getTag();
        getTeam().menu.make(mContextObject, menu, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return getTeam().menu.choose(this, mContextObject, item);
    }

    @Override
    public void onBackPressed() {
        if (getTeam().camera.isOpen()) {
            getTeam().camera.close();
        } else {
            Fragment slide = mSlideScreen.getSlideFragment(mSlideScreen.getSlide());
            if (!(slide instanceof OnBackPressed) || !((OnBackPressed) slide).onBackPressed()) {
                super.onBackPressed();
            }
        }
    }
}
