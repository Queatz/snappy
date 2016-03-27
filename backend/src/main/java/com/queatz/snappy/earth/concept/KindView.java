package com.queatz.snappy.earth.concept;

import com.queatz.snappy.earth.view.ExistenceView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jacob on 3/26/16.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface KindView {
    Class<? extends ExistenceView> value();
}
