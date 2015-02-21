package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PersonMessagesAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Message;

import io.realm.RealmResults;

/**
 * Created by jacob on 10/26/14.
 */
public class PersonMessagesSlide extends Fragment {
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
        View view = inflater.inflate(R.layout.person_messages, container, false);

        final Team team = ((MainApplication) getActivity().getApplication()).team;

        final ListView list = (ListView) view.findViewById(R.id.messagesList);

        if(mPerson != null) {
            RealmResults<Message> messages = team.realm.where(Message.class)
                    .beginGroup()
                        .equalTo("from.id", team.auth.getUser())
                        .equalTo("to.id", mPerson.getId())
                    .endGroup()
                    .or()
                    .beginGroup()
                        .equalTo("from.id", mPerson.getId())
                        .equalTo("to.id", team.auth.getUser())
                    .endGroup()
                    .findAll();

            messages.sort("date", true);

            list.setAdapter(new PersonMessagesAdapter(getActivity(), messages, team.auth.me()));

            Log.w(Config.LOG_TAG, "Messages: " + list.getAdapter().getCount());

            final EditText writeMessage = (EditText) view.findViewById(R.id.writeMessage);
            final View sendButton = view.findViewById(R.id.sendButton);

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = writeMessage.getText().toString();

                    if (mPerson == null || message.trim().isEmpty())
                        return;

                    team.action.sendMessage(mPerson, message);

                    writeMessage.setText("");
                }
            });
        }

        return view;
    }
}
