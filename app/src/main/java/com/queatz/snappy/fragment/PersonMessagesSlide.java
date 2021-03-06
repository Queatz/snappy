package com.queatz.snappy.fragment;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PersonMessagesAdapter;
import com.queatz.snappy.team.Camera;
import com.queatz.snappy.team.TeamFragment;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.SendMessageAction;
import com.queatz.snappy.team.observers.AnonymousEnvironment;
import com.queatz.snappy.team.observers.AuthenticatedEnvironment;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 10/26/14.
 */
public class PersonMessagesSlide extends TeamFragment {
    DynamicRealmObject mPerson;
    String messagePrefill;
    Uri image;

    public void setPerson(DynamicRealmObject person) {
        mPerson = person;
    }

    public void setMessagePrefill(String message) {
        messagePrefill = message;
        prefill(getView());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.person_messages, container, false);

        view.setFitsSystemWindows(true);

        if(mPerson == null) {
            return view;
        }

        final ListView list = (ListView) view.findViewById(R.id.messagesList);

        when(new AuthenticatedEnvironment() {
            @Override
            public void then() {
                RealmResults<DynamicRealmObject> messages = getTeam().realm.where("Thing")
                        .beginGroup()
                        .equalTo("from.id", getTeam().auth.me().getString(Thing.ID))
                        .equalTo("to.id", mPerson.getString(Thing.ID))
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("from.id", mPerson.getString(Thing.ID))
                        .equalTo("to.id", getTeam().auth.me().getString(Thing.ID))
                        .endGroup()
                        .sort(Thing.DATE, Sort.ASCENDING)
                        .findAll();

                list.setAdapter(new PersonMessagesAdapter(getActivity(), messages, getTeam().auth.me()));

            }
        });

        when(new AnonymousEnvironment() {
            @Override
            public void then() {
                list.setAdapter(null);
            }
        });

        final EditText writeMessage = (EditText) view.findViewById(R.id.writeMessage);
        final View sendButton = view.findViewById(R.id.sendButton);
        final View cameraButton = view.findViewById(R.id.cameraButton);

        prefill(view);

        writeMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        sendButton.callOnClick();
                }

                return true;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = writeMessage.getText().toString();

                if (mPerson == null || (message.trim().isEmpty() && image == null))
                    return;

                to(new SendMessageAction(mPerson, message, image));

                writeMessage.setText("");
                image = null;
                updateImageButton();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image == null) {
                    getPhoto();
                } else {
                    image = null;
                    Toast.makeText(getActivity(), getString(R.string.photo_removed), Toast.LENGTH_SHORT).show();
                    updateImageButton();
                }
            }
        });

        return view;
    }

    private void prefill(View view) {
        if (view == null) {
            return;
        }

        final EditText writeMessage = (EditText) view.findViewById(R.id.writeMessage);

        if (messagePrefill != null) {
            writeMessage.setText(messagePrefill);
            writeMessage.post(new Runnable() {
                @Override
                public void run() {
                    writeMessage.requestFocus();
                    writeMessage.selectAll();
                    getTeam().view.keyboard(writeMessage);
                }
            });
        }
    }

    public void updateImageButton() {
        if (getView() == null) {
            return;
        }

        int color = R.color.gray;

        if (image != null) {
            color = R.color.blue;
        }

        ((ImageButton) getView().findViewById(R.id.cameraButton))
                .setImageTintList(ColorStateList.valueOf(getResources().getColor(color)));
    }

    public void getPhoto() {
        getTeam().camera.getPhoto(getActivity(), new Camera.Callback() {
            @Override
            public void onPhoto(Uri uri) {
                PersonMessagesSlide.this.image = uri;
                updateImageButton();
            }

            @Override
            public void onClosed() {

            }
        });
    }
}
