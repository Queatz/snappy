package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.queatz.snappy.R;
import com.queatz.snappy.things.*;

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

        final ScrollView scroll = (ScrollView) view.findViewById(R.id.scroll);

        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });

        return view;
    }
}
