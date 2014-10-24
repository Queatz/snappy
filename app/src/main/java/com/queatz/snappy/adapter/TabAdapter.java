package com.queatz.snappy.adapter;

import android.content.Context;

import com.queatz.snappy.ui.ActionBar;

/**
 * Created by jacob on 10/19/14.
 */
public class TabAdapter extends ActionBar.TabAdapter {
    Context mContext;

    public TabAdapter(Context context) {
        mContext = context;
    }

    public int getCount() {
        return 2;
    }

    public String getTabName(int i) {
        return i == 0 ? "Upto" : "Into";
    }
}
