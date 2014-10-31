package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;

import com.queatz.snappy.fragment.ProfilePictureSlide;
import com.queatz.snappy.ui.SlideScreen;

/**
 * Created by jacob on 10/23/14.
 */
public class ProfilePictureAdapter extends SlideScreen.SlideScreenAdapter {
    public ProfilePictureAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public int getCount() {
        return 6;
    }

    public Fragment getSlide(int page) {
        ProfilePictureSlide slide = new ProfilePictureSlide();

        slide.setPage(page);

        return slide;
    }
}
