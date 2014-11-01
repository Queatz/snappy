package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.R;
import com.queatz.snappy.adapter.ProfilePictureAdapter;
import com.queatz.snappy.ui.CurrentSlideIndicator;
import com.queatz.snappy.ui.SlideScreen;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonUptoSlide extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_upto, container, false);

        final SlideScreen profileSlider = (SlideScreen) view.findViewById(R.id.profileSlider);

        final ProfilePictureAdapter adapter = new ProfilePictureAdapter(getFragmentManager());

        profileSlider.setAdapter(adapter);

        final CurrentSlideIndicator slideIndicator = (CurrentSlideIndicator) view.findViewById(R.id.slideIndicator);
        slideIndicator.setCount(adapter.getCount());
        slideIndicator.setOffset(0);

        profileSlider.setOnSlideCallback(new SlideScreen.OnSlideCallback() {
            @Override
            public void onSlide(int currentSlide, float offsetPercentage) {
                slideIndicator.setOffset(offsetPercentage);
            }

            @Override
            public void onSlideChange(int currentSlide) {

            }
        });

        return view;
    }
}
