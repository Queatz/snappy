package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import com.queatz.snappy.fragment.Into;
import com.queatz.snappy.fragment.Upto;
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
                return new Upto();
            case 1:
                return new Into();
            default:
                return null;
        }
    }
}
