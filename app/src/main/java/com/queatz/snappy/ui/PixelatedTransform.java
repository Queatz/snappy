package com.queatz.snappy.ui;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * Created by jacob on 9/17/17.
 */

public class PixelatedTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap bitmap = Bitmap.createScaledBitmap(source, source.getWidth() * 4, source.getHeight() * 4, false);
        source.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "pixelated";
    }
}
