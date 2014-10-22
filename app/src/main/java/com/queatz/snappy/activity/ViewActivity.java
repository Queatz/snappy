package com.queatz.snappy.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * Created by jacob on 10/19/14.
 */
public class ViewActivity extends Activity {
    private Fragment current;

    public void show(Fragment fragment) {
        current = fragment;

        if(fragment != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(android.R.id.content, fragment);
            transaction.commit();
        }
    }
}
