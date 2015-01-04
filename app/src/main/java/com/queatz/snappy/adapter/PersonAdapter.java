package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;

import com.queatz.snappy.fragment.MessagesSlide;
import com.queatz.snappy.fragment.PersonIntoSlide;
import com.queatz.snappy.fragment.PersonUptoSlide;
import com.queatz.snappy.ui.SlideScreen;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonAdapter extends SlideScreen.SlideScreenAdapter {
    public PersonAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public int getCount() {
        return 2;
    }

    public Fragment getSlide(int page) {
        switch (page) {
            case 0:
                return new PersonUptoSlide();
            case 1:
                return new MessagesSlide();
            default:
                return null;
        }
    }
}
