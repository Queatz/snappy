package com.queatz.snappy.adapter;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.queatz.snappy.R;

import java.util.List;

/**
 * Created by jacob on 9/18/17.
 */

public class AllAppsTopAdapter extends AllAppsBaseAdapter {

    public AllAppsTopAdapter(Context context, List<ResolveInfo> apps) {
        super(context, apps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.all_apps_top_item, parent, false);
        }

        ResolveInfo appInfo = getItem(position);
        view.setTag(appInfo);

        final ImageView appIcon = (ImageView) view.findViewById(R.id.appIcon);

        appIcon.setImageDrawable(appInfo.loadIcon(context.getPackageManager()));

        return view;
    }
}