package com.queatz.snappy.ui;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.queatz.snappy.R;

/**
 * Created by jacob on 9/24/17.
 */

public class ZoomableImageViewFragment extends Fragment {

    private Drawable drawable;
    private PhotoView photoView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.photoview, container, false);

        photoView = view.findViewById(R.id.photoView);

        if (drawable != null) {
            photoView.setImageDrawable(drawable);
        }

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZoomableImageView.close();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZoomableImageView.close();
            }
        });

        return view;
    }

    public ZoomableImageViewFragment setDrawable(Drawable drawable) {
        this.drawable = drawable.getCurrent();

        if (photoView != null) {
            photoView.setImageDrawable(drawable);
        }

        return this;
    }
}
