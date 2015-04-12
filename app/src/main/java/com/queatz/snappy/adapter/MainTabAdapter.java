package com.queatz.snappy.adapter;

import android.content.Context;

import com.queatz.snappy.R;
import com.queatz.snappy.ui.ActionBar;

/**
 * Created by jacob on 10/25/14.
 */
public class MainTabAdapter extends ActionBar.TabAdapter {
    Context mContext;

    public MainTabAdapter(Context context) {
        mContext = context;
    }

    public int getCount() {
        return 2;
    }

    public String getTabName(int i) {
        return new String[]{mContext.getString(R.string.parties), mContext.getString(R.string.messages)}[i];
    }
}
