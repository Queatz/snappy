package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;

import com.queatz.snappy.fragment.MapSlide;
import com.queatz.snappy.fragment.MessagesSlide;
import com.queatz.snappy.fragment.PartiesSlide;
import com.queatz.snappy.ui.SlideScreen;

/**
 * Created by jacob on 10/19/14.
 */
public class MainAdapter extends SlideScreen.SlideScreenAdapter {
    public MainAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public int getCount() {
        return 2;
    }

    public Fragment getSlide(int page) {
        switch (page) {
            case 0:
                return new PartiesSlide();
            case 1:
                return new MessagesSlide();
            default:
                return null;
        }
    }
}
