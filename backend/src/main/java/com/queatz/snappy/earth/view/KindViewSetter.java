package com.queatz.snappy.earth.view;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jacob on 3/27/16.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface KindViewSetter {
    String value();
}
