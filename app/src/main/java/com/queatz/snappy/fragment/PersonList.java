package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
public class PersonList extends Fragment {
    com.queatz.snappy.things.Person mPerson;
    boolean mShowFollowing;

    public void setPerson(Person mPerson) {
        this.mPerson = mPerson;
    }

    public void setShowFollowing(boolean showFollowing) {
        mShowFollowing = showFollowing;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_list, container, false);

        Team team = ((MainApplication) getActivity().getApplication()).team;

        if(mPerson != null) {
            RealmResults<Follow> results = team.realm.where(Follow.class)
                    .equalTo(mShowFollowing ? "person.id" : "following.id", mPerson.getId())
                    .findAll();

            PersonListAdapter adapter = new PersonListAdapter(getActivity(), results, mShowFollowing);

            ((ListView) view.findViewById(R.id.personList)).setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
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