package com.queatz.earth;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.FulltextIndexOptions;
import com.arangodb.model.GeoIndexOptions;
import com.arangodb.model.HashIndexOptions;
import com.arangodb.model.SkiplistIndexOptions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.queatz.earth.query.EarthQueryAppendFilter;
import com.queatz.earth.query.EarthQueryNearFilter;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Gateway;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.shared.earth.EarthRef;

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

            __arangoDatabase.createCollection(CLUB_RELATIONSHIPS, new CollectionCreateOptions().type(CollectionType.EDGES));
            __arangoDatabase.createGraph(CLUB_GRAPH, ImmutableSet.of(new EdgeDefinition()
                    .collection(CLUB_RELATIONSHIPS)
                    .from(DEFAULT_COLLECTION)
                    .to(DEFAULT_COLLECTION)));
        } catch (ArangoDBException ignored) {
            // Whatever
        }

        // Index commonly searched fields
        try {
            __arangoDatabase.collection(DEFAULT_COLLECTION).createHashIndex(ImmutableList.of(DEFAULT_FIELD_CONCLUDED), new HashIndexOptions());
            __arangoDatabase.collection(DEFAULT_COLLECTION).createHashIndex(ImmutableList.of(EarthField.KIND), new HashIndexOptions());
            __arangoDatabase.collection(DEFAULT_COLLECTION).createHashIndex(ImmutableList.of(EarthField.SOURCE), new HashIndexOptions());
            __arangoDatabase.collection(DEFAULT_COLLECTION).createHashIndex(ImmutableList.of(EarthField.TARGET), new HashIndexOptions());
            __arangoDatabase.collection(DEFAULT_COLLECTION).createHashIndex(ImmutableList.of(EarthField.HIDDEN), new HashIndexOptions());
            __arangoDatabase.collection(DEFAULT_COLLECTION).createHashIndex(ImmutableList.of(EarthField.TOKEN), new HashIndexOptions());
            __arangoDatabase.collection(DEFAULT_COLLECTION).createHashIndex(ImmutableList.of(EarthField.EMAIL), new HashIndexOptions());
            __arangoDatabase.collection(DEFAULT_COLLECTION).createSkiplistIndex(ImmutableList.of(EarthField.EMAIL), new SkiplistIndexOptions().sparse(true));

            __arangoDatabase.collection(DEFAULT_COLLECTION).createFulltextIndex(ImmutableList.of(
                    EarthField.NAME,
                    EarthField.FIRST_NAME,
                    EarthField.LAST_NAME,
                    EarthField.ABOUT
            ), new FulltextIndexOptions());
        } catch (ArangoDBException ignored) {
            // Whatever
        }

        QueryCachePropertiesEntity queryCachePropertiesEntity = new QueryCachePropertiesEntity();
        queryCachePropertiesEntity.setMode(QueryCachePropertiesEntity.CacheMode.on);

        __arangoDatabase.setQueryCacheProperties(queryCachePropertiesEntity);

        return __arangoDatabase;
    }

    public EarthStore(EarthAs as) {
        super(as);

        earthAuthority = use(EarthAuthority.class);

        this.db = getDb();

        this.collection = db.collection(DEFAULT_COLLECTION);
        this.collection.createGeoIndex(ImmutableSet.of(EarthField.GEO), new GeoIndexOptions());

        this.relationships = db.collection(DEFAULT_RELATIONSHIPS);
        this.clubRelationships = db.collection(CLUB_RELATIONSHIPS);
    }

    public static final String DEFAULT_GRAPH = "Graph";
    public static final String DEFAULT_COLLECTION = "Collection";
    public static final String DEFAULT_RELATIONSHIPS = "Relationships";
    public static final String CLUB_GRAPH = "ClubsGraph";
    public static final String CLUB_RELATIONSHIPS = "Clubs";

    public static final String DEFAULT_KIND_OWNER = EarthRelationship.OWNER;
    private static final String DEFAULT_FIELD_KIND = EarthField.KIND;
    private static final String DEFAULT_FIELD_CREATED = EarthField.CREATED_ON;
    public static final String DEFAULT_FIELD_CONCLUDED = "concluded_on";
    public static final String DEFAULT_FIELD_FROM = "_from";
    public static final String DEFAULT_FIELD_TO = "_to";
    public static final String DEFAULT_SORT = DEFAULT_FIELD_CREATED;

    static final Set<String> DEFAULT_AUTH_KINDS = ImmutableSet.of(
            EarthKind.PERSON_KIND, // Because people need to be created before logging in
            EarthKind.GEO_SUBSCRIBE_KIND,
            EarthKind.FORM_SUBMISSION_KIND,
            EarthKind.MEMBER_KIND
    );

    private final ArangoDatabase db;
    private final ArangoCollection collection;
    private final ArangoCollection relationships;
    private final ArangoCollection clubRelationships;

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
        ArangoCursor<BaseDocument> c = db.query(
                new EarthQuery(as)
                        .filter("_key", "'" + key.name() + "'")
                        .aql(),
                null,
                null,
                BaseDocument.class
        );

        if (!c.hasNext()) {
            return null;
        }

        entity = EarthThing.from(c.next());

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
            // Declare owner and make visible to self
            setOwner(thing, as.getUser());
            addToClub(thing, as.getUser());
        }

        return thing;
    }

    public void setOwner(@NotNull EarthThing thing, @NotNull EarthThing owner) {
        BaseDocument entity = new EarthThing.Builder()
                .set(DEFAULT_FIELD_KIND, DEFAULT_KIND_OWNER)
                .set(DEFAULT_FIELD_FROM, owner.id())
                .set(DEFAULT_FIELD_TO, thing.id())
                .build();

        relationships.insertDocument(entity, new DocumentCreateOptions());
    }

    public void addToClub(@NotNull EarthThing thing, @NotNull EarthThing club) {
        if (isInClub(thing, club)) {
            return;
        }

        BaseDocument entity = new EarthThing.Builder()
                .set(DEFAULT_FIELD_FROM, thing.id())
                .set(DEFAULT_FIELD_TO, club.id())
                .build();

        clubRelationships.insertDocument(entity, new DocumentCreateOptions());
    }

    /**
     * Get the owner.
     * @param thing
     */
    @Nullable
    public EarthThing ownerOf(@Nullable EarthThing thing) {
        if (thing == null) {
            return null;
        }

        ArangoCursor<BaseDocument> c = db.query(
                new EarthQuery(as)
                        .as("other, relationship")
                        .in("inbound @thing graph '" + DEFAULT_GRAPH + "'")
                        .filter("relationship." + DEFAULT_FIELD_KIND, "@kind")
                        .limit("1")
                        .aql("other"),
                ImmutableMap.of(
                        "thing", thing.id(),
                        "kind", DEFAULT_KIND_OWNER
                ),
                null,
                BaseDocument.class
        );

        if (c.hasNext()) {
            return EarthThing.from(c.next());
        } else {
            return null;
        }
    }

    public boolean isOwnerOf(@NotNull EarthThing entity, @NotNull EarthThing as) {
        EarthThing owner = ownerOf(entity);
        return owner != null && owner.id().equals(as.id());
    }

    public void removeFromClub(@NotNull EarthThing thing, @NotNull EarthThing club) {
        ArangoCursor<BaseDocument> c = db.query(
                "for x in " + CLUB_RELATIONSHIPS +
                        " filter x." + DEFAULT_FIELD_FROM + " == @thing" +
                        " and x." + DEFAULT_FIELD_TO + " == @club return x",
                ImmutableMap.of(
                        "thing", thing.id(),
                        "club", club.id()
                ),
                null,
                BaseDocument.class
        );

        while (c.hasNext()) {
            clubRelationships.deleteDocument(c.next().getKey());
        }
    }

    /**
     * Checks if a thing is in a club.
     * @param thing
     * @param club
     * @return True, if it is in the club
     */
    private boolean isInClub(EarthThing thing, EarthThing club) {
        ArangoCursor<BaseDocument> c = db.query(
                "for x in " + CLUB_RELATIONSHIPS +
                        " filter x." + DEFAULT_FIELD_FROM + " == @thing" +
                        " and x." + DEFAULT_FIELD_TO + " == @club limit 1 return x",
                ImmutableMap.of(
                        "thing", thing.id(),
                        "club", club.id()
                ),
                null,
                BaseDocument.class
        );

        return c.hasNext();
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
        EarthThing entity = get(id);

        // Don't allow concluding entities that have already concluded
        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
            return;
        }

        // XXX TODO Authorize

        // Remove club relationships
        use(ClubMine.class).clubsOf(entity)
                .forEach(club -> removeFromClub(entity, club));

        // Remove person from club
        if (EarthKind.MEMBER_KIND.equals(entity.getString(EarthField.KIND))) {
            EarthThing source = get(entity.getKey(EarthField.SOURCE));
            EarthThing target = get(entity.getKey(EarthField.TARGET));

            if (source != null && target != null) {
                if (EarthKind.PERSON_KIND.equals(source.getString(EarthField.KIND)) &&
                    EarthKind.CLUB_KIND.equals(target.getString(EarthField.KIND))) {
                        removeFromClub(source, target);
                }
            }
        }

        collection.updateDocument(
                entity.key().name(),
                entity.edit().set(DEFAULT_FIELD_CONCLUDED, new Date()).build()
        );
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

        if (entityBuilder.isCreate()) {
            if (!earthAuthority.authorize(thing, EarthRule.MODIFY)) {
                throw new NothingLogicResponse("unauthorized");
            }
        } else {
            if (!earthAuthority.authorize(thing, EarthRule.CREATE)) {
                throw new NothingLogicResponse("unauthorized (create)");
            }
        }

        // Don't allow concluding entities that have already concluded
        if (entity.getProperties().containsKey(DEFAULT_FIELD_CONCLUDED)) {
            return thing;
        }

        collection.updateDocument(entity.getKey(), entity);

        return thing;
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
        String aql = new EarthQuery(as)
                .filter(DEFAULT_FIELD_KIND, "'" + kind + "'")
                .filter(field, "'" + key.name() + "'")
                .count(true)
                .aql();

        return db.query(aql, ImmutableMap.of(), null, Integer.class).next();
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
        String aql = new EarthQuery(as)
                .filter(EarthField.KIND, "@kind")
                .filter("@field", "@key")
                .filter("@concluded_field", "null")
                .sort("x.@sort desc")
                .limit("@limit")
                .aql();

        Map<String, Object> vars = new HashMap<>();
        vars.put("kind", kind);
        vars.put("field", field);
        vars.put("key", key.name());
        vars.put("sort", DEFAULT_SORT);
        vars.put("limit", limit == null ? Config.NEARBY_MAX_COUNT : limit);
        vars.put("concluded_field", DEFAULT_FIELD_CONCLUDED);

        return queryRaw(aql, vars);
    }

    public List<EarthThing> queryRaw(String aql, Map<String, Object> vars) {
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
                        filter += "and date_timestamp(x." + EarthField.AROUND + ") >= date_timestamp(date_subtract(date_now(), " + TRANSIENT_KIND_TIMEOUT_SECONDS + ", 's'))";
                        filter += ")";
                    } else {
                        filter += "x.kind == \"" + kinds[i] + "\"";
                    }
                }

                filter += ")";
            }
        }

        final String Q_PARAM = "q";

        if (q != null) {
            filter += " and " + and(Q_PARAM);
        }

        String aql = new EarthQuery(as)
                .let("things", new EarthQueryNearFilter(as, "@latitude", "@longitude", "@limit").aql())
                .in(new EarthQueryAppendFilter("things", new EarthQuery(as)
                                .internal(true)
                                .as("thing")
                                .in("things " + new EarthQuery(as)
                                        .internal(true)
                                        .as("other, relationship")
                                        .in("outbound thing graph '" + DEFAULT_GRAPH + "'")
                                        .filter("relationship." + DEFAULT_FIELD_KIND, "@owner_kind")
                                        .aql("other")
                                ).aql(null)).aql())
                .filter(EarthField.KIND, "!=", "'" + EarthKind.DEVICE_KIND + "'")
                .filter(EarthField.KIND, "!=", "'" + EarthKind.GEO_SUBSCRIBE_KIND + "'")
                .filter(filter)
                .limit("@limit")
                .distinct(true)
                .aql();

        Map<String, Object> vars = new HashMap<>();
        vars.put("latitude", location.getLatitude());
        vars.put("longitude", location.getLongitude());
        vars.put("limit", Config.NEARBY_MAX_COUNT);
        vars.put("owner_kind", DEFAULT_KIND_OWNER);

        if (q != null) {
            vars.put(Q_PARAM, "%" + q.trim().toLowerCase() + "%");
        }

        ArangoCursor<BaseDocument> cursor = db.query(aql, vars, null, BaseDocument.class);

        List<EarthThing> result = new ArrayList<>();

        while (cursor.hasNext()) {
            result.add(EarthThing.from(cursor.next()));
        }

        return result;
    }

    private String and(String q) {
        return "(LOWER(x." + EarthField.NAME + ") like @" + q + " or " +
                "LOWER(x." + EarthField.FIRST_NAME + ") like @" + q + " or " +
                "LOWER(x." + EarthField.LAST_NAME + ") like @" + q + " or " +
                "LOWER(x." + EarthField.ABOUT + ") like @" + q + ")";
    }

    public List<EarthThing> query(String filter, @Nullable Map<String, Object> vars) {
        return query(filter, vars, -1);
    }

    public List<EarthThing> query(String filter, @Nullable Map<String, Object> filterVars, int limit) {
        return query(filter, filterVars, limit, null);
    }

    public List<EarthThing> query(String filter, @Nullable Map<String, Object> filterVars, int limit, String sort) {
        return query(false, filter, filterVars, limit, sort);
    }

    public List<EarthThing> query(boolean isInternalQuery, String filter, @Nullable Map<String, Object> filterVars, int limit, String sort) {

        Map<String, Object> var = new HashMap<>();

        if (filterVars != null) {
            var.putAll(filterVars);
        }

        var.put("limit", limit <= 0 ? Config.NEARBY_MAX_COUNT : limit);
        var.put("sort", sort == null ? DEFAULT_SORT : sort);

        String aql = new EarthQuery(as)
                .filter(filter)
                .sort("x.@sort desc")
                .limit("@limit")
                .internal(isInternalQuery)
                .aql();

        ArangoCursor<BaseDocument> cursor = db.query(aql, var, null, BaseDocument.class);

        List<EarthThing> result = new ArrayList<>();

        while (cursor.hasNext()) {
            result.add(EarthThing.from(cursor.next()));
        }

        return result;
    }

    /**
     * Skip visibility
     */
    public List<EarthThing> queryInternal(String filter, @Nullable Map<String, Object> filterVars, int limit) {
        return query(true, filter, filterVars, limit, null);
    }
}
