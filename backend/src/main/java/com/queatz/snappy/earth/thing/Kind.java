package com.queatz.snappy.earth.thing;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jacob on 3/26/16.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Kind {
    String value();
}
