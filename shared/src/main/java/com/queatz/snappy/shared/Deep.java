package com.queatz.snappy.shared;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jacob on 10/22/15.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Deep {
    int value() default 1;
}
