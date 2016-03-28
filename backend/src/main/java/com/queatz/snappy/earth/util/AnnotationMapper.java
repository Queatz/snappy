package com.queatz.snappy.earth.util;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by jacob on 3/27/16.
 *
 * @param <A> The annotation to map
 * @param <C> The class the annotation must associated with
 * @param <K> The resulting key
 * @param <V> The resulting value
 */
public interface AnnotationMapper<A extends Annotation, C, K, V> {
    Map.Entry<K, V> map(A annotation, C clazz);
}
