package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.MainAdapter;
import com.queatz.snappy.adapter.MainTabAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.ActionBar;
import com.queatz.snappy.ui.SlideScreen;
import com.queatz.snappy.things.Person;
import com.squareup.picasso.Picasso;

/**
 * Created by jacob on 10/19/14.
 */
public class Main extends Fragment {
    public Team team;

    private ActionBar mActionBar;
    private SlideScreen mSlideScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;

        Log.d(Config.LOG_TAG, "auth = " + team.auth.getAuthParam());
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

                team.action.openMinimenu(view);
            }
        });

        ImageView profile = ((ImageView) ((FrameLayout) mActionBar.findViewById(R.id.rightContent)).getChildAt(0));

        if(profile != null) {
            String usr = team.auth.getUser();

            if(usr != null) {
                Person person = team.things.get(Person.class, usr);

                if(person != null) {
                    Picasso.with(getActivity()).load(person.getImageUrlForSize((int) Util.px(getActivity(), 64))).placeholder(R.color.spacer).into(profile);
                }
            }
        }

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
