package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.PeopleTabAdapter;
import com.queatz.snappy.adapter.PersonAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.ActionBar;
import com.queatz.snappy.ui.SlideScreen;
import com.squareup.picasso.Picasso;

/**
 * Created by jacob on 10/19/14.
 */
public class Person extends Fragment {
    private ActionBar mActionBar;
    private SlideScreen mSlideScreen;
    private com.queatz.snappy.things.Person mPerson;

    public void setPerson(com.queatz.snappy.things.Person person) {
        mPerson = person;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person, container, false);

        mActionBar = (ActionBar) view.findViewById(R.id.actionBar);
        mActionBar.setAdapter(new PeopleTabAdapter(getActivity()));

        mActionBar.setLeftContent(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.pop();
            }
        });

        if(mPerson != null) {
            mActionBar.setTitle(mPerson.getName());

            ImageView profile = ((ImageView) mActionBar.getLeftContent().getChildAt(0));
            Picasso.with(getActivity())
                    .load(mPerson.getImageUrlForSize((int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into(profile);
        }

        mSlideScreen = (SlideScreen) view.findViewById(R.id.person_content);

        mSlideScreen.setAdapter(new PersonAdapter(getFragmentManager(), mPerson));
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
