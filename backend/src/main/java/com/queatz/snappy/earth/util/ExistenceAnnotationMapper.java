package com.queatz.snappy.earth.util;

import com.queatz.snappy.earth.thing.Existence;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by jacob on 3/26/16.
 */
public interface ExistenceAnnotationMapper<A extends Annotation, K, V> {
    Map.Entry<K, V> map(A annotation, Class<? extends Existence> clazz);
}
