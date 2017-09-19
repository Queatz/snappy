package com.queatz.snappy.adapter;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by jacob on 9/18/17.
 */

public abstract class AllAppsBaseAdapter extends BaseAdapter {

    protected Context context;
    protected List<ResolveInfo> apps;

    public AllAppsBaseAdapter(Context context, List<ResolveInfo> apps) {
        this.context = context;
        this.apps = apps;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public ResolveInfo getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
