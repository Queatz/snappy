package com.queatz.snappy.earth.util;

import com.google.common.reflect.Reflection;
import com.google.common.reflect.TypeToken;
import com.queatz.snappy.earth.thing.Existence;

import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 3/26/16.
 */
public class ExistenceAnnotationMap {

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <A extends Annotation, K, V> Map<K, V> create(ExistenceAnnotationMapper<A, K, V> mapper) {
        final Map<K, V> map = new HashMap<>();

        Reflections existables = new Reflections(Reflection.getPackageName(Existence.class));

        Class<A> annotation = (Class<A>) (new TypeToken<Class<A>>() {}.getType());

        Set<Class<?>> classes = existables.getTypesAnnotatedWith(annotation);

        for (Class<?> clazz : classes) {
            if (!Existence.class.isAssignableFrom(clazz)) {
                throw new RuntimeException("Class annotated is not an Existence! class=" + clazz);
            }

            Map.Entry<K, V> pair = mapper.map(clazz.getAnnotation(annotation), (Class<? extends Existence>) clazz);

            if (map.containsKey(pair.getKey())) {
                throw new RuntimeException("Multiple kind! class=" + clazz);
            }

            map.put(pair.getKey(), pair.getValue());
        }

        return map;
    }
}
