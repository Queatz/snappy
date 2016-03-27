package com.queatz.snappy.earth.concept;

import com.google.common.reflect.Reflection;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.queatz.snappy.earth.access.UnsupportedConceptEarthException;
import com.queatz.snappy.earth.access.As;
import com.queatz.snappy.earth.access.NothingEarthException;
import com.queatz.snappy.earth.thing.Existence;
import com.queatz.snappy.earth.thing.UpdateRelation;
import com.queatz.snappy.earth.util.ExistenceAnnotationMap;
import com.queatz.snappy.earth.util.KindExistenceAnnotationMapper;

import org.reflections.Reflections;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by jacob on 3/26/16.
 */
public class RepositoryConcept extends Concept {

    final static private Map<String, Class<? extends Existence>> existences =
            ExistenceAnnotationMap.create(new KindExistenceAnnotationMapper());

    static {

        Reflections existables = new Reflections(Reflection.getPackageName(Existence.class));

        Set<Class<?>> entityClasses = existables.getTypesAnnotatedWith(Entity.class);

        for (Class<?> clazz : entityClasses) {
            ObjectifyService.register(clazz);
        }
    }

    private static String newId() {
        // TODO make sure it's actually new = unique!
        return Long.toString(new Random().nextLong());
    }

    public Existence make(String kind) {
        Class<? extends Existence> clazz = existences.get(kind);

        return make(clazz);
    }

    public <T extends Existence> T make(Class<T> clazz) {
        T thing;

        try {
            thing = clazz.newInstance();
            thing.setCreated(new Date());
            thing.setId(newId());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return thing;
    }

    public class KindGetter<T extends Existence> {
        final Class<T> kind;

        private KindGetter(Class<T> kind) {
            this.kind = kind;
        }

        public T id(String id) {
            throw new NothingEarthException();

            // TODO datastore.get
            // if not visible to me, or not exists, throw new NothingEarthException()
        }
    }

    public class KindDeleter<T extends Existence> {
        final Class<T> kind;

        private KindDeleter(Class<T> kind) {
            this.kind = kind;
        }

        public T id(String id) {
            throw new NothingEarthException();
        }
    }

    public KindGetter get(String kind) {
        return get(existences.get(kind));
    }

    public KindGetter get(Class<? extends Existence> clazz) {
        if (clazz == null) {
            throw new UnsupportedConceptEarthException();
        }

        return new KindGetter<>(clazz);
    }

    public KindDeleter delete(String kind) {
        return delete(existences.get(kind));
    }

    public KindDeleter delete(Class<? extends Existence> clazz) {
        if (clazz == null) {
            throw new UnsupportedConceptEarthException();
        }

        return new KindDeleter<>(clazz);
    }

    private RepositoryConcept(As as) {
        super(as);
    }

    public void save(Existence thing) {

    }

    public void delete(Existence thing) {

    }

    public void delete(Key key) {

    }
}
