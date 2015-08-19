package com.queatz.snappy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PersonListAdapter;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Person;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by jacob on 1/4/15.
 */
public class PersonList extends Activity implements RealmChangeListener {
    com.queatz.snappy.things.Person mPerson;
    boolean mShowFollowing;
    Team team;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;
        Intent intent = getIntent();

        if(intent == null) {
            Log.w(Config.LOG_TAG, "Null intent");
            return;
        }

        mShowFollowing = intent.getBooleanExtra("showFollowing", false);
        String id = intent.getStringExtra("person");

        if(id == null) {
            Log.w(Config.LOG_TAG, "No person specified");
            return;
        }

        mPerson = team.realm.where(Person.class).equalTo("id", id).findFirst();

        setContentView(R.layout.person_list);
        onChange();
        fetchList();

        team.realm.addChangeListener(this);
    }

    @Override
    public void onChange() {
        ListView personAdapter = (ListView) findViewById(R.id.personList);

        if(mPerson != null && personAdapter != null) {
            RealmResults<Follow> results = team.realm.where(Follow.class)
                    .equalTo(mShowFollowing ? "person.id" : "following.id", mPerson.getId())
                    .findAll();

            final PersonListAdapter adapter = new PersonListAdapter(this, results, mShowFollowing);

            personAdapter.setAdapter(adapter);

            personAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    team.action.openProfile(PersonList.this, adapter.getPerson(position));
                }
            });
        }
    }

    private void fetchList() {
        team.api.get(mShowFollowing ? Config.PATH_PEOPLE_FOLLOWING : Config.PATH_PEOPLE_FOLLOWERS, null, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.putAll(Join.class, response);
            }

            @Override
            public void fail(String response) {

            }
        });
    }

    @Override
    public void onDestroy() {
        team.realm.removeChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}