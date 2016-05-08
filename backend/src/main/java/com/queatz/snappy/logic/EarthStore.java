package com.queatz.snappy.logic;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.DateTime;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.LatLng;
import com.google.cloud.datastore.NullValue;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.Transaction;
import com.queatz.snappy.shared.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 4/2/16.
 */
public class EarthStore {

    private static final String DEFAULT_KIND = "Thing";
    public static final String DEFAULT_FIELD_KIND = EarthField.KIND;
    public static final String DEFAULT_FIELD_CREATED = "created_on";
    public static final String DEFAULT_FIELD_CONCLUDED = "concluded_on";

    private final Datastore datastore = DatastoreOptions.defaultInstance().service();
    private final KeyFactory keyFactory = datastore.newKeyFactory().kind(DEFAULT_KIND);

    private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
    EarthSearcher earthSearcher = EarthSingleton.of(EarthSearcher.class);

    /**
     * Get a thing from the store.
     *
     * - Fails if the thing has already concluded.
     *
     * @return The thing, or null if it could not be found
     */
    public Entity get(@Nonnull String id) {
        Entity entity = datastore.get(keyFactory.newKey(id));

        if (entity == null) {
            return null;
        }

        // Entity has already concluded, don't allow access anymore
        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
            return null;
        }

        return entity;
    }

//    public Entity getWithAuthorities(@Nonnull String id, List<String> authorities) {
//        StructuredQuery.Builder<Entity> query = Query.entityQueryBuilder()
//                .kind(DEFAULT_KIND);
//
//        StructuredQuery.Filter concludedFilter = StructuredQuery.PropertyFilter
//                .eq(EarthStore.DEFAULT_FIELD_CONCLUDED, NullValue.of());
//
//
//        StructuredQuery.Filter idFilter = StructuredQuery.PropertyFilter
//                .eq(DEFAULT_KEY, keyFactory.newKey(id));
//
//        StructuredQuery.Filter authoritiesFilter = StructuredQuery.PropertyFilter
//                .eq(EarthField.AUTHORITIES, authorities);
//
//        query.filter(StructuredQuery.CompositeFilter.and(authoritiesFilter, concludedFilter, idFilter));
//
//        Entity entity = datastore.run(query.build());
//
//        if (entity == null) {
//            return null;
//        }
//
//        // Entity has already concluded, don't allow access anymore
//        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
//            return null;
//        }
//
//        return entity;
//    }

    /**
     * Put a thing into the store.
     *
     * - Does not modify the entity.
     * - Fails if the thing doesn't have a creation date.
     * - Fails if the thing has already concluded
     */
    public void put(@Nonnull Entity entity) {
        if (entity.key().name().isEmpty()) {
            return;
        }

        // Don't allow saving entities without a created date
        try {
            entity.getDateTime(DEFAULT_FIELD_CREATED);
        } catch (DatastoreException e) {
            e.printStackTrace();
            return;
        }

        // Don't allow saving concluded entities, use conclude() instead
        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
            return;
        }

        datastore.put(entity);
        earthSearcher.update(entity);
    }

    /**
     * Create a thing of the specified kind.
     *
     * - Creates a new id for the thing.
     * - Sets the creation date.
     *
     * @return The new thing
     */
    public Entity create(@Nonnull String kind) {
        Key key = keyFactory.newKey(newRandomId());
        Entity entity = Entity.builder(key)
                .set(DEFAULT_FIELD_CREATED, DateTime.now())
                .set(DEFAULT_FIELD_CONCLUDED, NullValue.of())
                .set(DEFAULT_FIELD_KIND, StringValue.of(kind))
                .build();
        datastore.put(entity);
        earthSearcher.update(entity);
        return entity;
    }

    /**
     * Conclude a thing by it's id.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public void conclude(@Nonnull String id) {
        Transaction transaction = datastore.newTransaction();

        try {
            Entity entity = transaction.get(keyFactory.newKey(id));

            // Don't allow concluding entities that have already concluded
            if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
                return;
            }

            transaction.put(Entity.builder(entity).set(DEFAULT_FIELD_CONCLUDED, DateTime.now()).build());
            transaction.commit();
            earthSearcher.delete(id);
        } finally {
            if (transaction.active()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Save a thing.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public Entity.Builder edit(@Nonnull Entity entity) {
        return Entity.builder(entity);
    }

    /**
     * Save a thing.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public Entity save(@Nonnull Entity.Builder entityBulder) {
        Entity entity = entityBulder.build();

        Transaction transaction = datastore.newTransaction();

        try {
            // Don't allow concluding entities that have already concluded
            if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
                return entity;
            }

            transaction.put(entity);
            transaction.commit();
            earthSearcher.update(entity);
        } finally {
            if (transaction.active()) {
                transaction.rollback();
            }
        }

        return entity;
    }

    public final String newRandomId() {
        return Long.toString(new Random().nextLong());
    }

    public List<Entity> getNearby(LatLng center, String kind) {
        return earthSearcher.getNearby(kind, center, 100);
    }

//    public List<Entity> queryNearToWithDatastore(GeoPt center, String kindFilter) {
//        // XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX
//        double radius = 11265;
//        Query.Filter containsFilter = new Query.StContainsFilter(EarthField.GEO, new Query.GeoRegion.Circle(center, radius));
//        Query.Filter filter;
//
//        // XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX
//        List<Query.Filter> conditions = new ArrayList<>();
//        conditions.add(new Query.FilterPredicate(EarthStore.DEFAULT_FIELD_CONCLUDED, Query.FilterOperator.EQUAL, null));
//        conditions.add(containsFilter);
//
//        if (kindFilter != null) {
//            conditions.add(new Query.FilterPredicate(EarthField.KIND, Query.FilterOperator.EQUAL, kindFilter));
//        }
//
//        // XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX
//        filter = new Query.CompositeFilter(Query.CompositeFilterOperator.AND, conditions);
//        List<com.google.appengine.api.datastore.Entity> entities = datastoreService
//                .prepare(new Query(DEFAULT_KIND).setFilter(filter))
//                .asList(FetchOptions.Builder.withDefaults());
//
//        // XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX
//        List<Entity> keys = new  ArrayList<>();
//        for (com.google.appengine.api.datastore.Entity entity : entities) {
//            keys.add(get(entity.getKey().getName()));
//        }
//
//        return keys;
//    }
}
