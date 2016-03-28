package com.queatz.snappy.earth.util;

import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jacob on 3/27/16.
 */
public class MethodAnnotationMap {
    public static <A extends Annotation, M extends Annotation, C, K> Map<K, Map<String, Method>> create(final Class<C> clazz, final Class<M> methodAnnotation, final AnnotationValueMapper<A, K> annotationValueMapper, final AnnotationValueMapper<M, String> methodAnnotationValueMapper) {
        return AnnotationMap.create(clazz, new AnnotationMapper<A, C, K, Map<String, Method>>() {

            @Override
            public Map.Entry<K, Map<String, Method>> map(A annotation, C clazz) {
                Map<String, Method> map = new HashMap<>();

                Set<Method> methods = new Reflections(clazz).getMethodsAnnotatedWith(methodAnnotation);

                for (Method method : methods) {
                    M a = method.getAnnotation(methodAnnotation);

                    if (a == null) {
                        throw new RuntimeException("Annotation not found.");
                    }

                    String value = methodAnnotationValueMapper.map(a);

                    if (map.containsKey(value)) {
                        throw new RuntimeException("Annotation value already declared. value=" + value);
                    }

                    map.put(value, method);
                }

                return Pair.of(annotationValueMapper.map(annotation), map);
            }
        });
    }
}
