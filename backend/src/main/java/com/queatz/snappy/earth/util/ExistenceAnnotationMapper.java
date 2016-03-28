package com.queatz.snappy.earth.util;

import com.queatz.snappy.earth.thing.Existence;

import java.lang.annotation.Annotation;

/**
 * Created by jacob on 3/26/16.
 */
public interface ExistenceAnnotationMapper<A extends Annotation, K, V> extends AnnotationMapper<A, Class<? extends Existence>, K, V> {}
