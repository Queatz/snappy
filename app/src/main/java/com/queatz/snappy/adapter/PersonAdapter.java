package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import com.queatz.snappy.fragment.PersonInto;
import com.queatz.snappy.fragment.PersonUpto;
import com.queatz.snappy.ui.SlideScreen;
import com.queatz.snappy.ui.SlideScreen.SlideScreenAdapter;

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
                return new PersonUpto();
            case 1:
                return new PersonInto();
            default:
                return null;
        }
    }
}
