package com.queatz.snappy.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.queatz.snappy.R;
import com.queatz.snappy.adapter.AllAppsAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.TeamFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jacob on 9/18/17.
 */

public class AppsSlide extends TeamFragment {

    private final List<ResolveInfo> allApps = new ArrayList<>();
    private AllAppsAdapter allAppsAdapter;
    private PackageManager packageManager;
    private PreferenceManager.OnActivityResultListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageManager = getActivity().getPackageManager();

        listener = new PreferenceManager.OnActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == Config.REQUEST_CODE_APP_LIST_CHANGED) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populate();
                            allAppsAdapter.notifyDataSetChanged();
                        }
                    });
                    return true;
                }

                return false;
            }
        };

        getTeam().callbacks.set(Config.REQUEST_CODE_APP_LIST_CHANGED, listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getTeam().callbacks.unset(Config.REQUEST_CODE_APP_LIST_CHANGED, listener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.all_apps, container, false);

        populate();

        allAppsAdapter = new AllAppsAdapter(getActivity(), allApps);

        ListView allAppsListView = (ListView) view.findViewById(R.id.allApps);
        allAppsListView.setAdapter(allAppsAdapter);

        allAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ResolveInfo appInfo = allAppsAdapter.getItem(position);
                launch(appInfo);
            }
        });

        return view;
    }

    private void populate() {
        allApps.clear();

        Intent intent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);

        final String selfPackage = getActivity().getPackageName();

        for (ResolveInfo app : packages) {
            if (selfPackage.equals(app.activityInfo.packageName)) {
                continue;
            }

            allApps.add(app);
        }

        Collections.sort(allApps, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo o1, ResolveInfo o2) {
                return o1.loadLabel(packageManager).toString().compareTo(o2.loadLabel(packageManager).toString());
            }
        });
    }

    private boolean isSystem(ResolveInfo app) {
        return ((app.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    private Intent getIntent(ResolveInfo resolveInfo) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.setClassName(
                resolveInfo.activityInfo.applicationInfo.packageName,
                resolveInfo.activityInfo.name
        );

        return intent;
    }

    private void launch(ResolveInfo app) {
        Intent intent = getIntent(app);

        if (intent == null) {
            return;
        }

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.app_not_installed, Toast.LENGTH_SHORT).show();
        }
    }
}
