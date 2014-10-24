package com.queatz.snappy.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.transition.Fade;
import android.view.animation.Animation;

import com.queatz.snappy.R;

/**
 * Created by jacob on 10/19/14.
 */
public class ViewActivity extends Activity {
    private Fragment current;

    public void show(Fragment fragment) {
        current = fragment;

        if(fragment != null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        }
    }
}
