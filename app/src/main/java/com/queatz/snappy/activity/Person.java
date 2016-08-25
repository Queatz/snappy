package com.queatz.snappy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.PeopleTabAdapter;
import com.queatz.snappy.adapter.PersonAdapter;
import com.queatz.snappy.adapter.ProfileAdapter;
import com.queatz.snappy.adapter.ProfileTabAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.ActionBar;
import com.queatz.snappy.ui.SlideScreen;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/19/14.
 */
public class Person extends Activity {
    private ActionBar mActionBar;
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

        String id = intent.getStringExtra("person");

        if(id == null) {
            Log.w(Config.LOG_TAG, "No person specified");
            return;
        }

        mPerson = team.realm.where("Thing").equalTo("id", id).findFirst();
        // Else load person and wait

        setContentView(R.layout.person);

        mActionBar = (ActionBar) findViewById(R.id.actionBar);

        boolean itsMe = team.auth.getUser() != null && team.auth.getUser().equals(mPerson.getString(Thing.ID));

        if (itsMe) {
            mActionBar.setAdapter(new ProfileTabAdapter(this));
        } else {
            mActionBar.setAdapter(new PeopleTabAdapter(this));
        }

        mActionBar.setLeftContent(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.navigateUpFromSameTask(Person.this);
            }
        });

        if(mPerson != null) {
            mActionBar.setTitle(Functions.getFullName(mPerson));

            ImageView profile = ((ImageView) mActionBar.getLeftContent().getChildAt(0));
            Picasso.with(this)
                    .load(Functions.getImageUrlForSize(mPerson, (int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into(profile);
        }

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
                mActionBar.setSlide(offset);
            }

            @Override
            public void onSlideChange(int slide) {
                getWindow().setSoftInputMode(slide == 1 ? WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE : WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                if(mPerson != null) {
                    if (slide == 1 && mIsActive) {
                        team.action.setSeen(mPerson);
                        team.push.clear("messages");
                        team.view.setTop("person/" + mPerson.getString(Thing.ID) + "/messages");
                    } else {
                        team.view.clearTop("person/" + mPerson.getString(Thing.ID) + "/messages");
                    }
                }

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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String show = intent.getStringExtra("show");

        if(show == null) {
            mActionBar.resolve();
            return;
        }

        mActionBar.setPage("upto".equals(show) ? 0 : "messages".equals(show) ? 1 : 0);
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
        mContextObject = view.getTag();
        team.menu.make(mContextObject, menu, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return team.menu.choose(this, mContextObject, item);
    }
}
