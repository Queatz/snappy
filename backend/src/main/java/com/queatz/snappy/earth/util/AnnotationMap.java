package com.queatz.snappy.earth.util;

import com.google.common.reflect.Reflection;
import com.google.common.reflect.TypeToken;
import com.queatz.snappy.earth.thing.Existence;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 3/27/16.
 */
public class AnnotationMap {

    /**
     * Create a new annotations map.
     *
     * @param packageClass A class, used to refernece the package to scan
     * @param mapper The mapper to use
     * @param <A> The annotation to look for
     * @param <C> The class type the annotation must be associated with
     * @param <K> The resulting map key type
     * @param <V> The resulting map value type
     * @return The resulting map
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <A extends Annotation, C, K, V> Map<K, V> create(Class<?> packageClass, AnnotationMapper<A, C, K, V> mapper) {
        final Map<K, V> map = new HashMap<>();
        final Reflections existables = new Reflections(Reflection.getPackageName(packageClass));
        final Class<A> annotation = (Class<A>) (new TypeToken<Class<A>>() {}.getType());
        final Class<C> expecting = (Class<C>) (new TypeToken<Class<C>>() {}.getType());
        final Set<Class<?>> classes = existables.getTypesAnnotatedWith(annotation);

        for (Class<?> clazz : classes) {
            if (!expecting.isAssignableFrom(clazz)) {
                throw new RuntimeException("Class annotated is wrong type! class=" + clazz);
            }

            Map.Entry<K, V> pair = mapper.map(clazz.getAnnotation(annotation), (C) clazz);

            if (map.containsKey(pair.getKey())) {
                throw new RuntimeException("Multiple kind! class=" + clazz);
            }

            map.put(pair.getKey(), pair.getValue());
        }

        return map;
    }
}
