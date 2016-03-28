package com.queatz.snappy.earth.util;

/**
 * Created by jacob on 3/27/16.
 */

import java.lang.annotation.Annotation;

public interface AnnotationValueMapper<A extends Annotation, V> {
    V map(A annotation);
}
