package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.queatz.snappy.MainActivity;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.TabAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.ActionBar;

/**
 * Created by jacob on 10/19/14.
 */
public class Person extends Fragment {
    private ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person, container, false);

        actionBar = (ActionBar) view.findViewById(R.id.actionBar);
        actionBar.setAdapter(new TabAdapter(getActivity()));
        actionBar.setTitle("Amanda Zhang");
        actionBar.setLeftContent(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.show(((MainActivity) team.view).mMainView);
            }
        });

        actionBar.setOnPageChangeListener(new ActionBar.OnPageChangeListener() {
            @Override
            public void onPageChange(int page) {
                if(getView() == null)
                    return;

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(page == 0 ? R.layout.person_upto : R.layout.person_into, (FrameLayout) getView().findViewById(R.id.content), true);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        actionBar.setPage(0);
    }
}
