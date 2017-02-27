package com.queatz.snappy.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PersonAdapter;
import com.queatz.snappy.adapter.ProfileAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.OnBackPressed;
import com.queatz.snappy.ui.SlideScreen;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/19/14.
 */
public class Person extends FullscreenActivity {
    private SlideScreen mSlideScreen;
    private DynamicRealmObject mPerson;
    private boolean mIsActive;
    public Team team;
    private Object mContextObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = ((MainApplication) getApplication()).team;

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

        mPerson = team.realm.where("Thing").equalTo(Thing.ID, id).findFirst();
        // Else load person and wait

        if(mPerson == null) {
            Log.w(Config.LOG_TAG, "Person with id wasn't found in local database");
            return;
        }

        setContentView(R.layout.person);

        boolean itsMe = team.auth.getUser() != null && team.auth.getUser().equals(mPerson.getString(Thing.ID));

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
                    if (slide == 1 && mIsActive) {
                        team.action.setSeen(mPerson);
                        team.push.clear("messages");
                        team.view.setTop("person/" + mPerson.getString(Thing.ID) + "/messages");
                    } else {
                        team.view.clearTop("person/" + mPerson.getString(Thing.ID) + "/messages");
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

        mSlideScreen.setSlide("upto".equals(show) ? 0 : "messages".equals(show) ? 1 : 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        mIsActive = true;

        if(mPerson != null && mSlideScreen.getSlide() == 1) {
            team.view.setTop("person/" + mPerson.getString(Thing.ID) + "/messages");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mIsActive = false;

        if (mPerson != null) {
            team.view.clearTop("person/" + mPerson.getString(Thing.ID) + "/messages");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
