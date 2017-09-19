package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;

import com.queatz.snappy.fragment.AppsSlide;
import com.queatz.snappy.fragment.ChatSlide;
import com.queatz.snappy.fragment.MessagesSlide;
import com.queatz.snappy.fragment.PartiesSlide;
import com.queatz.snappy.ui.slidescreen.SlideScreen;

/**
 * Created by jacob on 10/19/14.
 */
public class MainAdapter extends SlideScreen.SlideScreenAdapter {
    public MainAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public int getCount() {
        return 4;
    }

    public Fragment getSlide(int page) {
        switch (page) {
            case 0:
                return new ChatSlide();
            case 1:
                return new PartiesSlide();
            case 2:
                return new MessagesSlide();
            case 3:
                return new AppsSlide();
            default:
                return null;
        }
    }
}
