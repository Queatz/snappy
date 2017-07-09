package com.queatz.snappy.logic;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.GeoIndexOptions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Gateway;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * Created by jacob on 4/2/16.
 *
 * EarthStore + Mines are the only classes that access the raw underlying data store.
 */
public class EarthStore extends EarthControl {
    private final EarthAuthority earthAuthority;

    private static ArangoDatabase __arangoDatabase;

    private static ArangoDatabase getDb() {
        if (__arangoDatabase != null) {
            return __arangoDatabase;
        }

        __arangoDatabase = new ArangoDB.Builder()
                .user(Gateway.ARANGO_USER)
                .password(Gateway.ARANGO_PASSWORD)
                .build()
                .db();

        try {
            __arangoDatabase.createCollection(DEFAULT_COLLECTION);
            __arangoDatabase.createCollection(DEFAULT_RELATIONSHIPS, new CollectionCreateOptions().type(CollectionType.EDGES));
            __arangoDatabase.createGraph(DEFAULT_GRAPH, ImmutableSet.of(new EdgeDefinition()
                    .collection(DEFAULT_RELATIONSHIPS)
                    .from(DEFAULT_COLLECTION)
                    .to(DEFAULT_COLLECTION)));
        } catch (ArangoDBException ignored) {
            // Whatever
        }

        return __arangoDatabase;
    }

    public EarthStore(EarthAs as) {
        super(as);

        earthAuthority = use(EarthAuthority.class);

        this.db = getDb();

        this.collection = db.collection(DEFAULT_COLLECTION);
        this.relationships = db.collection(DEFAULT_RELATIONSHIPS);
        this.collection.createGeoIndex(ImmutableSet.of(EarthField.GEO), new GeoIndexOptions());
    }

    private static final String DEFAULT_GRAPH = "Graph";
    private static final String DEFAULT_COLLECTION = "Collection";
    private static final String DEFAULT_RELATIONSHIPS = "Relationships";
    private static final String DEFAULT_KIND = "Thing";
    private static final String DEFAULT_KIND_OWNER = "owner";
    private static final String DEFAULT_FIELD_KIND = EarthField.KIND;
    private static final String DEFAULT_FIELD_CREATED = EarthField.CREATED_ON;
    private static final String DEFAULT_FIELD_CONCLUDED = "concluded_on";
    private static final String DEFAULT_FIELD_FROM = "_from";
    private static final String DEFAULT_FIELD_TO = "_to";
    private static final Set<String> DEFAULT_AUTH_KINDS = ImmutableSet.of(
            EarthKind.PERSON_KIND, // Because people need to be created before logging in
            EarthKind.GEO_SUBSCRIBE_KIND
    );

    private final ArangoDatabase db;
    private final ArangoCollection collection;
    private final ArangoCollection relationships;

    public EarthThing get(@NotNull String id) {
        return get(new EarthRef(id));
    }

    /**
     * Get a thing from the store.
     *
     * - Fails if the thing has already concluded.
     *
     * @return The thing, or null if it could not be found
     */
    public EarthThing get(@NotNull EarthRef key) {
        EarthThing entity;

        if (as.__entityCache.containsKey(key)) {
            entity = as.__entityCache.get(key);
        } else {
            entity = EarthThing.from(collection.getDocument(key.name(), BaseDocument.class));

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
    public void put(@NotNull EarthThing entity) {
        if (entity.key().name().isEmpty()) {
            return;
        }

        // Don't allow saving entities without a created date
        if (!entity.has(DEFAULT_FIELD_CREATED)) {
            return;
        }

        // Don't allow saving concluded entities, use conclude() instead
        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
            return;
        }

        collection.insertDocument(entity.getRaw());
    }

    /**
     * Create a thing of the specified kind.
     *
     * - Creates a new id for the thing.
     * - Sets the creation date.
     *
     * @return The new thing
     */
    public EarthThing create(@NotNull String kind) {
        if (!DEFAULT_AUTH_KINDS.contains(kind)) {
            as.requireUser();
        }

        BaseDocument entity = new EarthThing.Builder()
                .set(DEFAULT_FIELD_CREATED, new Date())
                .set(DEFAULT_FIELD_CONCLUDED)
                .set(DEFAULT_FIELD_KIND, kind)
                .build();
        EarthThing thing = new EarthThing(collection.insertDocument(entity, new DocumentCreateOptions().returnNew(true).waitForSync(true)).getNew());

        if (as.hasUser()) {
            setOwner(thing, as.getUser());
        }

        return thing;
    }

    private void setOwner(@NotNull EarthThing thing, @NotNull EarthThing owner) {
        BaseDocument entity = new EarthThing.Builder()
                .set(DEFAULT_FIELD_KIND, DEFAULT_KIND_OWNER)
                .set(DEFAULT_FIELD_FROM, DEFAULT_COLLECTION + "/" + owner.key().name())
                .set(DEFAULT_FIELD_TO, DEFAULT_COLLECTION + "/" + thing.key().name())
                .build();

        relationships.insertDocument(entity, new DocumentCreateOptions());
    }

    /**
     * Create a relationship between two things.
     * @param thing
     * @param fromThing
     * @param kind
     */
    public void join(@NotNull EarthThing thing, @NotNull EarthThing fromThing, @NotNull String kind) {
        BaseDocument entity = new EarthThing.Builder()
                .set(DEFAULT_FIELD_KIND, kind)
                .set(DEFAULT_FIELD_FROM, DEFAULT_COLLECTION + "/" + fromThing.key().name())
                .set(DEFAULT_FIELD_TO, DEFAULT_COLLECTION + "/" + thing.key().name())
                .build();

        relationships.insertDocument(entity, new DocumentCreateOptions());
    }

    /**
     * Conclude a thing by it's id.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public void conclude(@NotNull String id) {

//        XXX TODO
//        if (!earthAuthority.authorize(entity, as, EarthRule.MODIFY)) {
//            return null;
//        }
//
        EarthThing entity = get(id);

        // Don't allow concluding entities that have already concluded
        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
            return;
        }

        // XXX TODO Authorize

        collection.updateDocument(
                entity.key().name(),
                entity.edit().set(DEFAULT_FIELD_CONCLUDED, new Date()).build()
        );

        as.__entityCache.put(entity.key(), entity);
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
    public EarthThing.Builder edit(@NotNull EarthThing entity) {

        if (!earthAuthority.authorize(entity, EarthRule.MODIFY)) {
            throw new NothingLogicResponse("unauthorized");
        }

        return entity.edit();
    }

    /**
     * Save a thing.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public EarthThing save(@NotNull EarthThing.Builder entityBuilder) {
        BaseDocument entity = entityBuilder.build();
        EarthThing thing = new EarthThing(entity);

        if (!earthAuthority.authorize(thing, EarthRule.MODIFY)) {
            throw new NothingLogicResponse("unauthorized");
        }

        // Don't allow concluding entities that have already concluded
        if (entity.getProperties().containsKey(DEFAULT_FIELD_CONCLUDED)) {
            return thing;
        }

        collection.updateDocument(entity.getKey(), entity);

        as.__entityCache.put(thing.key(), thing);

        return thing;
    }

    /**
     * Find relationships.//
     */
    public List<EarthThing> findFor(EarthThing thing, String kind, String relationshipKind) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", DEFAULT_COLLECTION + "/" + thing.key().name());
        vars.put("kind", DEFAULT_KIND_OWNER);
        vars.put("concluded_field", DEFAULT_FIELD_CONCLUDED);

        String filter = "";

        if (relationshipKind != null) {
            filter += "and relationship.kind == @relationshipKind ";
            vars.put("relationshipKind", relationshipKind);
        }

        if (kind != null) {
            filter += "and other.kind == @kind ";
            vars.put("kind", kind);
        }

        String aql = "for other, relationship in outbound @key graph '" + DEFAULT_GRAPH + "' " +
                "filter other.@concluded_field == null " + filter + "return distinct other";

        Logger.getLogger(Config.NAME).info(aql);
        ArangoCursor<BaseDocument> cursor = db.query(aql, vars, null, BaseDocument.class);

        List<EarthThing> result = new ArrayList<>();

        while (cursor.hasNext()) {
            result.add(EarthThing.from(cursor.next()));
        }

        return result;
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
        String aql = "return count(for x in " + DEFAULT_COLLECTION + " filter x.kind == @kind and x.@field == @key and x.@concluded_field == null return 1)";

        Map<String, Object> vars = ImmutableMap.<String, Object>of(
                "kind", kind,
                "field", field,
                "key", key.name(),
                "concluded_field", DEFAULT_FIELD_CONCLUDED
        );

        return db.query(aql, vars, null, Integer.class).next();
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
        String aql = "for x in " + DEFAULT_COLLECTION + " " +
                "filter x.kind == @kind and x.@field == @key and x.@concluded_field == null " +
                "sort x.@sort " +
                "limit @limit " +
                "return x";

        Map<String, Object> vars = new HashMap<>();
        vars.put("kind", kind);
        vars.put("field", field);
        vars.put("key", key.name());
        vars.put("sort", EarthField.CREATED_ON);
        vars.put("limit", limit == null ? Config.NEARBY_MAX_COUNT : limit);
        vars.put("concluded_field", DEFAULT_FIELD_CONCLUDED);

        Logger.getLogger(Config.NAME).info(aql);
        ArangoCursor<BaseDocument> cursor = db.query(aql, vars, null, BaseDocument.class);

        List<EarthThing> result = new ArrayList<>();

        while (cursor.hasNext()) {
            result.add(EarthThing.from(cursor.next()));
        }

        return result;
    }

    public List<EarthThing> find(String kind, String field, EarthRef key) {
        return find(kind, field, key, null);
    }

    // These kinds disappear from the searcher after the specified time of inactivity
    private static final Collection TRANSIENT_KINDS = ImmutableSet.of(EarthKind.PERSON_KIND);
    private static final long TRANSIENT_KIND_TIMEOUT_SECONDS = 60 * 60 * 24 * 5; // 5 days

    public List<EarthThing> getNearby(EarthGeo center, String kind, String q) {
        return getNearby(kind, q, center, null);
    }

    public List<EarthThing> getNearby(EarthGeo center, String kind, boolean recent, String q) {
        return getNearby(kind, q, center, recent ? new Date(new Date().getTime() - 1000 * 60 * 60) : null);
    }

    private List<EarthThing> getNearby(String kind, String q, EarthGeo location, Date afterDate) {
        String filter = "";

        if (afterDate != null) {
            filter += "x." + DEFAULT_FIELD_CREATED + " >= " + (afterDate.getTime());
        }

        if (kind != null) {
            String kinds[] = kind.split(Pattern.quote("|"));

            if (kinds.length > 0 && !kinds[0].isEmpty()) {
                if (!filter.isEmpty()) {
                    filter += " and ";
                }

                filter += "(";

                for (int i = 0; i < kinds.length; i++) {
                    if (i > 0) {
                        filter += " or ";
                    }

                    if (TRANSIENT_KINDS.contains(kinds[i])) {
                        filter += "(";
                        filter += "x.kind == \"" + kinds[i] + "\" ";
                        filter += "and date_timestamp(x." + EarthField.AROUND + ") >= date_timestamp(date_subtract(date_now(), " + TRANSIENT_KIND_TIMEOUT_SECONDS + "), 's')";
                        filter += ")";
                    } else {
                        filter += "x.kind == \"" + kinds[i] + "\"";
                    }
                }

                filter += ")";
            }
        }

        if (q != null) {
            String[] qs = q.split("\\s+");

            if (qs.length == 1) {
                filter += " and " + and(q);
            } else if (qs.length > 1) {
                filter += " and (" + and(qs[0]);

                for (int i = 1; i < qs.length; i++) {
                    filter += " or " + and(qs[i]);
                }

                filter += ")";
            }

        }

        filter = (!Strings.isNullOrEmpty(filter) ? filter + " and " : "");

        boolean searchWithLinks = true;

        // TODO - Only allow chosen types
        String aql = "let things = (for x in near(" + DEFAULT_COLLECTION + ", @latitude, @longitude, @limit) return x) " +
                "for x in " +
                (searchWithLinks ? "append(things, (for thing in things for other, relationship in outbound thing graph '" + DEFAULT_GRAPH + "' filter relationship.kind == @owner_kind return other)) " : "things") +
                "filter " + filter + "x.@concluded_field == null and x.kind != 'device' " +
                "limit @limit " +
                "return distinct x";
        Map<String, Object> vars = new HashMap<>();
        vars.put("latitude", location.getLatitude());
        vars.put("longitude", location.getLongitude());
        vars.put("limit", Config.NEARBY_MAX_COUNT);
        vars.put("owner_kind", DEFAULT_KIND_OWNER);
        vars.put("concluded_field", DEFAULT_FIELD_CONCLUDED);

        Logger.getLogger(Config.NAME).info(aql);
        ArangoCursor<BaseDocument> cursor = db.query(aql, vars, null, BaseDocument.class);

        List<EarthThing> result = new ArrayList<>();

        while (cursor.hasNext()) {
            result.add(EarthThing.from(cursor.next()));
        }

        return result;
    }

    private String and(String q) {
        return "(x.name like '%" + q + "%' or " +
                "x.firstName like '%" + q + "%' or " +
                "x.lastName like '%" + q + "%' or " +
                "x.about like '%" + q + "%')";
    }

    public List<EarthThing> query(String filter, @Nullable Map<String, Object> vars) {
        return query(filter, vars, -1);
    }

    public List<EarthThing> query(String filter, @Nullable Map<String, Object> filterVars, int limit) {
        return query(filter, filterVars, limit, null);
    }

    public List<EarthThing> query(String filter, @Nullable Map<String, Object> filterVars, int limit, String sort) {

        Map<String, Object> var = new HashMap<>();

        if (filterVars != null) {
            var.putAll(filterVars);
        }

        var.put("_limit", limit <= 0 ? Config.NEARBY_MAX_COUNT : limit);
        var.put("_sort_by", limit <= 0 ? Config.NEARBY_MAX_COUNT : limit);
        var.put("_concluded_on", DEFAULT_FIELD_CONCLUDED);

        String aql = "for x in " + DEFAULT_COLLECTION + " " +
                "filter " + filter + " and x.@_concluded_on == null " +
                "limit @_limit " +
                (sort != null ? "sort x.@_sort_by " : "sort x." + EarthField.CREATED_ON) +
                "return x";

        Logger.getLogger(Config.NAME).info(aql);

        ArangoCursor<BaseDocument> cursor = db.query(aql, var, null, BaseDocument.class);

        List<EarthThing> result = new ArrayList<>();

        while (cursor.hasNext()) {
            result.add(EarthThing.from(cursor.next()));
        }

        return result;
    }
}
