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

    public static final int CHAT_SLIDE = 0;
    public static final int MAP_SLIDE = 1;
    public static final int MESSAGES_SLIDE = 2;
    public static final int APPS_SLIDE = 3;

    public MainAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public int getCount() {
        return 4;
    }

    public Fragment getSlide(int page) {
        switch (page) {
            case CHAT_SLIDE:
                return new ChatSlide();
            case MAP_SLIDE:
                return new PartiesSlide();
            case MESSAGES_SLIDE:
                return new MessagesSlide();
            case APPS_SLIDE:
                return new AppsSlide();
            default:
                return null;
        }
    }
}
