package com.queatz.snappy.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PersonListAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.*;
import com.queatz.snappy.things.Person;

import io.realm.RealmResults;

/**
 * Created by jacob on 1/4/15.
 */
public class PersonList extends Activity {
    com.queatz.snappy.things.Person mPerson;
    boolean mShowFollowing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Team team = ((MainApplication) getApplication()).team;
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

        if(mPerson != null) {
            setContentView(R.layout.person_list);

            RealmResults<Follow> results = team.realm.where(Follow.class)
                    .equalTo(mShowFollowing ? "person.id" : "following.id", mPerson.getId())
                    .findAll();

            final PersonListAdapter adapter = new PersonListAdapter(this, results, mShowFollowing);

            ListView personAdapter = (ListView) findViewById(R.id.personList);

            personAdapter.setAdapter(adapter);

            personAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    team.action.openProfile(PersonList.this, adapter.getPerson(position));
                }
            });
        }
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