package com.queatz.snappy.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.R;
import com.queatz.snappy.team.TeamFragment;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatSlide extends TeamFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat, container, false);

        return view;
    }
}
