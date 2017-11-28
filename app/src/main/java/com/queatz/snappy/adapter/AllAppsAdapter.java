package com.queatz.snappy.adapter;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.R;
import com.queatz.snappy.util.AppImages;

import java.util.List;

/**
 * Created by jacob on 9/18/17.
 */

public class AllAppsAdapter extends AllAppsBaseAdapter {

    private AppImages appImages;

    public AllAppsAdapter(Context context, List<ResolveInfo> apps) {
        super(context, apps);
        appImages = new AppImages();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.all_apps_item, parent, false);
        }

        ResolveInfo appInfo = getItem(position);
        view.setTag(appInfo);

        final TextView appName = view.findViewById(R.id.appName);
        final ImageView appIcon = view.findViewById(R.id.appIcon);

        appIcon.setImageDrawable(null);
        appName.setText("");
        appImages.loadIcon(appIcon, appInfo);
        appImages.loadLabel(appName, appInfo);

        return view;
    }
}
