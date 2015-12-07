package com.queatz.snappy.adapter;

import android.content.Context;

import com.queatz.snappy.R;
import com.queatz.snappy.ui.ActionBar;

/**
 * Created by jacob on 12/6/15.
 */
public class ProfileTabAdapter extends ActionBar.TabAdapter {
    Context mContext;

    public ProfileTabAdapter(Context context) {
        mContext = context;
    }

    public int getCount() {
        return 2;
    }

    public String getTabName(int i) {
        return new String[]{mContext.getString(R.string.about), mContext.getString(R.string.settings)}[i];
    }
}
