package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.MainActivity;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.adapter.MainAdapter;
import com.queatz.snappy.adapter.MainTabAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.ActionBar;
import com.queatz.snappy.ui.SlideScreen;

/**
 * Created by jacob on 10/19/14.
 */
public class Main extends Fragment {
    private ActionBar mActionBar;
    private SlideScreen mSlideScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main, container, false);

        mActionBar = (ActionBar) view.findViewById(R.id.actionBar);
        mActionBar.setAdapter(new MainTabAdapter(getActivity()));
        mActionBar.setRightContent(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.push(ViewActivity.Transition.SEXY_PROFILE, ViewActivity.Transition.IN_THE_VOID, ((MainActivity) team.view).mPersonView);
            }
        });

        mSlideScreen = (SlideScreen) view.findViewById(R.id.main_content);
        mSlideScreen.setAdapter(new MainAdapter(getFragmentManager()));

        mSlideScreen.setOnSlideCallback(new SlideScreen.OnSlideCallback() {
            @Override
            public void onSlide(int currentSlide, float offset) {
                mActionBar.setSlide(offset);
            }

            @Override
            public void onSlideChange(int slide) {
                mActionBar.selectPage(slide);
            }
        });

        mActionBar.setOnPageChangeListener(new ActionBar.OnPageChangeListener() {
            @Override
            public void onPageChange(int page) {
                mSlideScreen.setSlide(page);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mActionBar.setPage(0);
    }
}
