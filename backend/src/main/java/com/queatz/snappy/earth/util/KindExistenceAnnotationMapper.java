package com.queatz.snappy.earth.util;

import com.queatz.snappy.earth.thing.Existence;
import com.queatz.snappy.earth.thing.Kind;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * Created by jacob on 3/27/16.
 */
public class KindExistenceAnnotationMapper implements ExistenceAnnotationMapper<Kind, String, Class<? extends Existence>> {

    @Override
    public Map.Entry<String, Class<? extends Existence>> map(Kind annotation, Class<? extends Existence> clazz) {
        return Pair.<String, Class<? extends Existence>>of(annotation.value(), clazz);
    }
}