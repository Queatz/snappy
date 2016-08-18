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

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.LikerAdapter;
import com.queatz.snappy.adapter.PersonListAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 1/4/15.
 */
public class PersonList extends Activity {
    DynamicRealmObject mPerson;
    DynamicRealmObject mUpdate;
    DynamicRealmObject mOffer;
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

        setContentView(R.layout.person_list);
        final ListView personAdapter = (ListView) findViewById(R.id.personList);

        boolean showLikers = intent.getBooleanExtra("showLikers", false);

        if (showLikers) {
            String id = intent.getStringExtra("update");

            if (id == null) {
                Log.w(Config.LOG_TAG, "No update specified");
                return;
            }

            mUpdate = team.realm.where("Thing")
                    .equalTo(Thing.ID, id)
                    .findFirst();

            if(mUpdate != null && personAdapter != null) {
                RealmResults<DynamicRealmObject> results = team.realm.where("Thing")
                        .equalTo(Thing.KIND, "like")
                        .equalTo("target.id", mUpdate.getString(Thing.ID))
                        .findAll();

                final LikerAdapter adapter = new LikerAdapter(this, results);

                personAdapter.setAdapter(adapter);

                personAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        team.action.openProfile(PersonList.this, adapter.getPerson(position));
                    }
                });
            }

            fetchLikers();
        } else {
            mShowFollowing = intent.getBooleanExtra("showFollowing", false);
            String id = intent.getStringExtra("person");

            if (id == null) {
                Log.w(Config.LOG_TAG, "No person specified");
                return;
            }

            mPerson = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "person")
                    .equalTo("id", id)
                    .findFirst();

            if(mPerson != null && personAdapter != null) {
                RealmResults<DynamicRealmObject> results = team.realm.where("Thing")
                        .equalTo(Thing.KIND, "follower")
                        .equalTo(mShowFollowing ? "source.id" : "target.id", mPerson.getString(Thing.ID))
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

            fetchList();
        }
    }

    private void fetchLikers() {
        team.api.get(Config.PATH_EARTH + "/" + mUpdate.getString(Thing.ID) + "/" + Config.PATH_LIKERS, null, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.putAll(response);
            }

            @Override
            public void fail(String response) {

            }
        });
    }

    private void fetchList() {
        team.api.get(Config.PATH_EARTH + "/" + String.format(mShowFollowing ? Config.PATH_PEOPLE_FOLLOWING : Config.PATH_PEOPLE_FOLLOWERS, mPerson.getString(Thing.ID)), null, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.putAll(response);
            }

            @Override
            public void fail(String response) {

            }
        });
    }

    @Override
    public void onDestroy() {
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