package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.R;
import com.queatz.snappy.ui.SlideScreen;

/**
 * Created by jacob on 10/19/14.
 */
public class MainAdapter implements SlideScreen.ScreenAdapter {
    Context mContext;

    public MainAdapter(Context context) {
        mContext = context;
    }

    public int getCount() {
        return 2;
    }

    public View getView(int page, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(page == 0 ? R.layout.upto : R.layout.into, null, false);

        return view;
    }
}
