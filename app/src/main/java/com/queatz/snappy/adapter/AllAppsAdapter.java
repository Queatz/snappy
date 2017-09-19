package com.queatz.snappy.adapter;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.R;

import java.util.List;

/**
 * Created by jacob on 9/18/17.
 */

public class AllAppsAdapter extends BaseAdapter {

    private Context context;
    private List<ResolveInfo> apps;

    public AllAppsAdapter(Context context, List<ResolveInfo> apps) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.app_item, parent, false);
        }

        ResolveInfo appInfo = getItem(position);

        final TextView appName = (TextView) view.findViewById(R.id.appName);
        final ImageView appIcon = (ImageView) view.findViewById(R.id.appIcon);

        appName.setText(appInfo.loadLabel(context.getPackageManager()));
        appIcon.setImageDrawable(appInfo.loadIcon(context.getPackageManager()));

        return view;
    }
}
