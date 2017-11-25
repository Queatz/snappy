package com.queatz.snappy.util;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 11/18/17.
 */

public class AppImages {

    private final Map<Integer, AsyncTask> targets;

    public AppImages() {
        this.targets = new HashMap<>();
    }


    public void loadIcon(final ImageView imageView, final ResolveInfo resolveInfo) {
        final int i = imageView.hashCode();

        if (targets.containsKey(i)) {
            targets.get(i).cancel(true);
        }

        AsyncTask<Void, Void, Drawable> asyncTask = new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void[] voids) {
                return  resolveInfo.loadIcon(imageView.getContext().getPackageManager());
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                if (isCancelled()) {
                    return;
                }

                targets.remove(i);

                imageView.setImageDrawable(drawable);
            }
        };

        targets.put(i, asyncTask);

        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void loadLabel(final TextView textView, final ResolveInfo resolveInfo) {
        final int i = textView.hashCode();

        if (targets.containsKey(i)) {
            targets.get(i).cancel(true);
        }

        AsyncTask<Void, Void, CharSequence> asyncTask = new AsyncTask<Void, Void, CharSequence>() {
            @Override
            protected CharSequence doInBackground(Void[] voids) {
                return resolveInfo.loadLabel(textView.getContext().getPackageManager());
            }

            @Override
            protected void onPostExecute(CharSequence charSequence) {
                if (isCancelled()) {
                    return;
                }

                targets.remove(i);

                textView.setText(charSequence);
            }
        };

        targets.put(i, asyncTask);

        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
