package com.queatz.snappy.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;

import com.queatz.snappy.R;

/**
 * Created by jacob on 9/24/17.
 */

public class ZoomableImageView {

    private static ZoomableImageViewFragment frag;
    private static Activity activity;

    public static void zoomable(final ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isZooming()) {
                    close();
                } else {
                    open(imageView);
                }
            }
        });
    }

    public static boolean isZooming() {
        return frag != null;
    }

    public static void open(ImageView imageView) {
        activity = ((Activity) imageView.getContext());
        View cameraLayout = activity.findViewById(R.id.cameraLayout);

        frag = new ZoomableImageViewFragment().setDrawable(imageView.getDrawable());

        cameraLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.add(R.id.cameraLayout, frag, null);
        transaction.commitAllowingStateLoss();
    }

    public static void close() {
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.remove(frag);
        transaction.commitAllowingStateLoss();
        activity.findViewById(R.id.cameraLayout).setVisibility(View.GONE);

        activity = null;
        frag = null;
    }
}
