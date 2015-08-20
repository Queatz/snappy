package com.queatz.snappy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.PeopleTabAdapter;
import com.queatz.snappy.adapter.PersonAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.ActionBar;
import com.queatz.snappy.ui.SlideScreen;
import com.squareup.picasso.Picasso;

/**
 * Created by jacob on 10/19/14.
 */
public class Person extends Activity {
    private ActionBar mActionBar;
    private SlideScreen mSlideScreen;
    private com.queatz.snappy.things.Person mPerson;
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

        mPerson = team.realm.where(com.queatz.snappy.things.Person.class).equalTo("id", id).findFirst();
        // Else load person and wait

        setContentView(R.layout.person);

        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setAdapter(new PeopleTabAdapter(this));

        mActionBar.setLeftContent(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if(mPerson != null) {
            mActionBar.setTitle(mPerson.getName());

            ImageView profile = ((ImageView) mActionBar.getLeftContent().getChildAt(0));
            Picasso.with(this)
                    .load(mPerson.getImageUrlForSize((int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into(profile);
        }

        mSlideScreen = (SlideScreen) findViewById(R.id.person_content);

        mSlideScreen.setAdapter(new PersonAdapter(getFragmentManager(), mPerson));
        mSlideScreen.setOnSlideCallback(new SlideScreen.OnSlideCallback() {
            @Override
            public void onSlide(int currentSlide, float offset) {
                mActionBar.setSlide(offset);
            }

            @Override
            public void onSlideChange(int slide) {
                if(mPerson != null && slide == 1 && mIsActive) {
                    team.action.setSeen(mPerson);
                    team.push.clear("person/" + mPerson.getId() + "/messages");
                    team.view.setTop("person/" + mPerson.getId() + "/messages");
                }
                else {
                    team.view.clearTop("person/" + mPerson.getId() + "/messages");
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

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String show = intent.getStringExtra("show");

        mActionBar.setPage(show == null || "upto".equals(show) ? 0 : "messages".equals(show) ? 1 : 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        mIsActive = true;

        if(mPerson != null && mSlideScreen.getSlide() == 1) {
            team.view.setTop("person/" + mPerson.getId() + "/messages");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mIsActive = false;

        team.view.clearTop("person/" + mPerson.getId() + "/messages");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        team.action.onActionResult(this, requestCode, resultCode, data);
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
