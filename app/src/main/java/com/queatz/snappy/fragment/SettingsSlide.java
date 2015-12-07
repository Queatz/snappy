package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PersonMessagesAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Message;

import io.realm.RealmResults;

/**
 * Created by jacob on 12/6/15.
 */
public class SettingsSlide extends Fragment {
    com.queatz.snappy.things.Person mPerson;

    public void setPerson(com.queatz.snappy.things.Person person) {
        mPerson = person;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);

        final Team team = ((MainApplication) getActivity().getApplication()).team;

        return view;
    }
}
