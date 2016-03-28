package com.queatz.snappy.earth.util;

import com.queatz.snappy.earth.thing.Existence;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 3/26/16.
 */
public class ExistenceAnnotationMap {

    @Nonnull
    public static <A extends Annotation, K, V> Map<K, V> create(ExistenceAnnotationMapper<A, K, V> mapper) {
        return AnnotationMap.create(Existence.class, mapper);
    }
}
