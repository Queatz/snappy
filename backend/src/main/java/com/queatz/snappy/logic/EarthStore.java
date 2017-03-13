package com.queatz.snappy.logic;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

/**
 * Created by jacob on 4/2/16.
 *
 * EarthStore + Mines are the only classes that access the raw underlying data store.
 */
public class EarthStore extends EarthControl {
    private final EarthAuthority earthAuthority;
    private final EarthSearcher earthSearcher;

    public EarthStore(EarthAs as) {
        super(as);

        earthAuthority = use(EarthAuthority.class);
        earthSearcher = use(EarthSearcher.class);

        Cache cache;
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            Map properties = new HashMap<>();
            properties.put(GCacheFactory.EXPIRATION_DELTA, TimeUnit.DAYS.toSeconds(1));
            cache = cacheFactory.createCache(properties);
        } catch (CacheException e) {
            cache = null;
        }

        this.cache = cache;
    }

    private static final String DEFAULT_KIND = "Thing";
    private static final String DEFAULT_FIELD_KIND = EarthField.KIND;
    private static final String DEFAULT_FIELD_CREATED = EarthField.CREATED_ON;
    private static final String DEFAULT_FIELD_CONCLUDED = "concluded_on";

    private final Datastore datastore = DatastoreOptions.defaultInstance().service();
    private final KeyFactory keyFactory = datastore.newKeyFactory().kind(DEFAULT_KIND);
    private final Cache cache;

    public EarthThing get(@Nonnull String id) {
        return get(keyFactory.newKey(id));
    }

    public EarthThing get(@Nonnull EarthRef ref) {
        return get(keyFactory.newKey(id));
    }

    private Transaction transaction = null;

    public void transact() {
        transaction = getDatastore().newTransaction();
    }

    public void commit() {
        transaction.commit();
        transaction = null;
    }

    /**
     * Get a thing from the store.
     *
     * - Fails if the thing has already concluded.
     *
     * @return The thing, or null if it could not be found
     */
    public EarthThing get(@Nonnull EarthRef key) {
        EarthThing entity;

        if (as.__entityCache.containsKey(key)) {
            entity = as.__entityCache.get(key);
        } else {
            if (cache.containsKey(key)) {
                entity = (EarthThing) cache.get(key);
            } else {
                entity = (transaction != null ? transaction.get(key) : datastore.get(key));

                if (entity != null) {
                    cache.put(key, entity);
                }
            }

            // Can be null
            as.__entityCache.put(key, entity);
        }

        if (entity == null) {
            return null;
        }

        // Entity has already concluded, don't allow access anymore
        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
            return null;
        }

        if (!earthAuthority.authorize(entity, EarthRule.ACCESS)) {
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
    public void put(@Nonnull EarthThing entity) {
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
    public EarthThing create(@Nonnull String kind) {
        EarthRef key = keyFactory.newKey(newRandomId());
        EarthThing entity = EarthThing.builder(key)
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

//        XXX TODO
//        if (!earthAuthority.authorize(entity, as, EarthRule.MODIFY)) {
//            return null;
//        }
//
        Transaction transaction = datastore.newTransaction();

        try {
            EarthThing entity = get(keyFactory.newKey(id));

            // Don't allow concluding entities that have already concluded
            if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
                return;
            }

            // XXX TODO Authorize

            transaction.put(EarthThing.builder(entity).set(DEFAULT_FIELD_CONCLUDED, new Date()).build());
            transaction.commit();
            earthSearcher.delete(id);

            as.__entityCache.put(entity.key(), entity);
            cache.put(entity.key(), entity);
        } finally {
            if (transaction.active()) {
                transaction.rollback();
            }
        }
    }

    public void conclude(EarthThing entity) {

        if (!earthAuthority.authorize(entity, EarthRule.MODIFY)) {
            return;
        }

        conclude(entity.key().name());
    }

    /**
     * Save a thing.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public EarthThing.Builder edit(@Nonnull EarthThing entity) {

        if (!earthAuthority.authorize(entity, EarthRule.MODIFY)) {
            throw new NothingLogicResponse("unauthorized");
        }

        return EarthThing.builder(entity);
    }

    /**
     * Save a thing.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public EarthThing save(@Nonnull EarthThing.Builder entityBuilder) {
        EarthThing entity = entityBuilder.build();

        if (!earthAuthority.authorize(entity, EarthRule.MODIFY)) {
            throw new NothingLogicResponse("unauthorized");
        }

        Transaction transaction = datastore.newTransaction();

        try {
            // Don't allow concluding entities that have already concluded
            if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
                return entity;
            }

            transaction.put(entity);
            transaction.commit();
            earthSearcher.update(entity);

            as.__entityCache.put(entity.key(), entity);
            cache.put(entity.key(), entity);
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

    /**
     * Count how many things match a query.
     *
     * @param kind The kind of thing to count
     * @param field The field to equate
     * @param key The value the field should be
     * @return Number of matching things
     */
    public int count(String kind, String field, EarthRef key) {
        Query query = Query.keyQueryBuilder().kind(DEFAULT_KIND)
                .filter(StructuredQuery.CompositeFilter.and(
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, kind),
                        StructuredQuery.PropertyFilter.eq(field, key),
                        StructuredQuery.PropertyFilter.eq(EarthStore.DEFAULT_FIELD_CONCLUDED, null)
                ))
                .build();

        return count(datastore.run(query));
    }

    public int count(Iterator queryResults) {
        return Iterators.size(queryResults);
    }

    /**
     * Find things matching a query.
     *
     * @param kind The kind of thing to count
     * @param field The field to equate
     * @param key The key the field should contain
     * @return All the things
     */
    public List<EarthThing> find(String kind, String field, EarthRef key, Integer limit) {
        Query<EarthThing> query = Query.entityQueryBuilder().kind(DEFAULT_KIND)
                .filter(StructuredQuery.CompositeFilter.and(
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, kind),
                        StructuredQuery.PropertyFilter.eq(field, key),
                        StructuredQuery.PropertyFilter.eq(EarthStore.DEFAULT_FIELD_CONCLUDED, null)
                ))
                .limit(limit)
                .orderBy(StructuredQuery.OrderBy.desc(EarthField.CREATED_ON))
                .build();

        return Lists.newArrayList(datastore.run(query));
    }

    public List<EarthThing> find(String kind, String field, EarthRef key) {
        return find(kind, field, key, null);
    }

    public List<EarthThing> getNearby(EarthGeo center, String kind) {
        return earthSearcher.getNearby(kind, null, center, null, 100);
    }

    public List<EarthThing> getNearby(EarthGeo center, String kind, String q) {
        return earthSearcher.getNearby(kind, q, center, null, 100);
    }

    public List<EarthThing> getNearby(EarthGeo center, String kind, boolean recent, String q) {
        return earthSearcher.getNearby(kind, q, center, recent ? new Date(new Date().getTime() - 1000 * 60 * 60) : null , 100);
    }

    public List<EarthThing> query(StructuredQuery.Filter... filters) {
        StructuredQuery.Filter filter = StructuredQuery.PropertyFilter.eq(EarthStore.DEFAULT_FIELD_CONCLUDED, null);

        StructuredQuery.Filter composite = StructuredQuery.CompositeFilter.and(filter, filters);

//        XXX Can do this when invalidation is implemented for ket+val+limit+kind matches
//        if (cache.containsKey(composite)) {
//            return (ArrayList<Entity>) cache.get(composite);
//        }

        List<EarthThing> results = Lists.newArrayList(datastore.run(StructuredQuery.entityQueryBuilder()
                .kind(DEFAULT_KIND)
                .filter(composite).build()));

//        cache.put(composite, results);

        return results;
    }

    public List<EarthThing> queryLimited(int limit, StructuredQuery.Filter... filters) {
        StructuredQuery.Filter filter = StructuredQuery.PropertyFilter.eq(EarthStore.DEFAULT_FIELD_CONCLUDED, null);

        StructuredQuery.Filter composite = StructuredQuery.CompositeFilter.and(filter, filters);

        return Lists.newArrayList(datastore.run(StructuredQuery.entityQueryBuilder()
                .kind(DEFAULT_KIND)
                .orderBy(StructuredQuery.OrderBy.desc(EarthField.CREATED_ON))
                .limit(limit)
                .filter(composite).build()));
    }

    public EarthRef key(String keyName) {
        return keyFactory.newKey(keyName);
    }

    // !!!!!!!!!!!!!!!!!!!!!!
// XXX TODO When datastore supports geo
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
