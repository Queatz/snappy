package com.queatz.snappy.logic;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableSet;
import com.queatz.snappy.shared.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jacob on 11/17/14.
 */
public class EarthSearcher extends EarthControl {

    // These kinds disappear from the searcher after the specified time of inactivity
    private static final Collection TRANSIENT_KINDS = ImmutableSet.of(EarthKind.PERSON_KIND);
    private static final long TRANSIENT_KIND_TIMEOUT_SECONDS = 60 * 60 * 24 * 5; // 5 days

    public EarthSearcher(final EarthAs as) {
        super(as);

        IndexSpec indexSpec = IndexSpec.newBuilder().setName("Thing").build();
        index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    private Index index;

    @Deprecated
    public boolean update(EarthThing object) {
        boolean success = updateGeo(object);
        updateLinked(object);
        return success;
    }

    @Deprecated
    private boolean updateGeo(EarthThing object) {
        Document document = build(object);

        if (document == null) {
            return false;
        }

        try {
            index.put(document);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Deprecated
    public void delete(EarthThing object) {
        index.delete(getId(object));
    }

    @Deprecated
    public void delete(String id) {
        index.delete(id);
    }

    @Deprecated
    public String getId(EarthThing object) {
        return object.key().name();
    }

    public List<EarthThing> getNearby(String kind, String q, EarthGeo location, Date afterDate, int count) {
        String queryString = "(";

        final String geoString = "geopoint(" + location.getLatitude() + ", " + location.getLongitude() + ")";

        queryString += "distance(geo, " + geoString + ") < " + Config.SEARCH_MAX_VISIBILITY;

        boolean searchWithLinks = true;

        if (searchWithLinks) {
            queryString += " OR distance(source_geo, " + geoString + ") < " + Config.SEARCH_MAX_VISIBILITY;
            queryString += " OR distance(target_geo, " + geoString + ") < " + Config.SEARCH_MAX_VISIBILITY;
        }

        queryString += ")";

        if (afterDate != null) {
            queryString += " AND created_on >= " + (afterDate.getTime() / 1000);
        }

        if (kind != null) {
            String kinds[] = kind.split(Pattern.quote("|"));
            queryString += " AND (";

            for (int i = 0; i < kinds.length; i++) {
                if (i > 0) {
                    queryString += " OR ";
                }

                if (TRANSIENT_KINDS.contains(kinds[i])) {
                    queryString += "(";
                    queryString += "kind = \"" + kinds[i] + "\"";
                    queryString += " AND updated_on >= " + Long.toString(new Date().getTime() / 1000 - TRANSIENT_KIND_TIMEOUT_SECONDS) + ")";
                } else {
                    queryString += "kind = \"" + kinds[i] + "\"";
                }
            }

            queryString += ")";
        }

        if (q != null) {
            String[] qs = q.split("\\s+");

            if (qs.length == 1) {
                queryString += " AND name = \"" + q + "\"";
            } else if (qs.length > 1) {
                queryString += " AND (name = \"" + qs[0] + "\"";

                for (int i = 1; i < qs.length; i++) {
                    queryString += " OR name = \"" + qs[i] + "\"";
                }

                queryString += ")";
            }

        }

        SortOptions sortOptions = SortOptions.newBuilder().addSortExpression(
                SortExpression.newBuilder().setExpression("distance(geo, geopoint(" + location.getLatitude() + ", " + location.getLongitude() + "))")
                        .setDirection(SortExpression.SortDirection.ASCENDING).build()
        ).build();

        QueryOptions queryOptions = QueryOptions.newBuilder().setSortOptions(sortOptions).setLimit(count).build();
        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

        List<EarthThing> results = new ArrayList<>();

        for(ScoredDocument document : index.search(query)) {
            EarthThing entity = thingFromDocument(document);

            if (entity != null) {
                results.add(entity);
            }
        }

        return results;
    }

    @Deprecated
    private EarthThing thingFromDocument(Document document) {
        final EarthStore earthStore = use(EarthStore.class);
        return earthStore.get(document.getId());
    }

    @Deprecated
    private Document build(EarthThing object) {
        final EarthStore earthStore = use(EarthStore.class);

        Document.Builder builder = Document.newBuilder();

        if (object.has(EarthField.GEO)) {
            EarthGeo latLng = object.getGeo(EarthField.GEO);

            Field geoField = Field.newBuilder().setName(EarthField.GEO)
                    .setGeoPoint(new GeoPoint(latLng.getLatitude(), latLng.getLongitude())).build();

            builder.addField(geoField);
        }

        Field kindField = Field.newBuilder().setName(EarthField.KIND)
                .setAtom(object.getString(EarthField.KIND)).build();

        builder.addField(kindField);
        builder.setId(getId(object));

        Field createdField = Field.newBuilder().setName(EarthField.CREATED_ON)
                .setNumber(object.getDate(EarthField.CREATED_ON).getTime() / 1000)
                .build();

        builder.addField(createdField);

        if (object.has(EarthField.AROUND)) {
            Field updatedField = Field.newBuilder().setName(EarthField.UPDATED_ON)
                    .setNumber(object.getDate(EarthField.AROUND).getTime() / 1000)
                    .build();

            builder.addField(updatedField);
        }

        if (object.has(EarthField.NAME) && object.getString(EarthField.NAME) != null) {
            Field nameField = Field.newBuilder().setName(EarthField.NAME)
                    .setText(tokenizeName(object.getString(EarthField.NAME))).build();

            builder.addField(nameField);
        }
        else if (object.has(EarthField.FIRST_NAME)) {
            String name = object.getString(EarthField.FIRST_NAME);

            if (object.has(EarthField.LAST_NAME)) {
                name += " " + object.getString(EarthField.LAST_NAME);
            }

            Field nameField = Field.newBuilder().setName(EarthField.NAME)
                    .setText(tokenizeName(name)).build();

            builder.addField(nameField);
        }

        // Add linked geo's
        // Fields source, target, source_geo, target_geo
        for (String field : new String[] { EarthField.SOURCE, EarthField.TARGET }) {
            if (object.has(field)) {
                EarthRef linkedEntityId = object.getKey(field);
                EarthThing linkedEntity = earthStore.get(linkedEntityId);

                if (linkedEntity != null) {
                    Field linkedId = Field.newBuilder().setName(field)
                            .setAtom(linkedEntityId.name()).build();

                    builder.addField(linkedId);

                    if (linkedEntity.has(EarthField.GEO)) {
                        EarthGeo linkedLatLng = linkedEntity.getGeo(EarthField.GEO);

                        Field linkedGeo = Field.newBuilder().setName(field + "_" + EarthField.GEO)
                                .setGeoPoint(new GeoPoint(linkedLatLng.getLatitude(), linkedLatLng.getLongitude())).build();

                        builder.addField(linkedGeo);
                    }
                }
            }
        }

        return builder.build();
    }

    // XXX slow, and sluggish
    // Updates the linked locations of linked entities
    @Deprecated
    private void updateLinked(EarthThing entity) {
        final EarthStore earthStore = use(EarthStore.class);

        String id = entity.key().name();

        String queryString = "source = \"" + id + "\" OR target = \"" + id + "\"";

        for(ScoredDocument document : index.search(Query.newBuilder().build(queryString))) {
            updateGeo(earthStore.get(document.getId())); // XXX TODO sadly, cascading dependencies will not be updated
        }
    }

    // Splits name into tokens based on the start of words
    // Example: "Jeff Lange" -> "J Je Jef Jeff L La Lan Lang Lange"
    private String tokenizeName(String name) {
        StringBuilder stringBuilder = new StringBuilder();

        String[] tokens = name.split("\\s+");
        for (String token : tokens) {
            for (int i = 1; i < token.length() + 1; i++) {
                stringBuilder.append(" ");
                stringBuilder.append(token.substring(0, i));
            }
        }

        return stringBuilder.toString();
    }
}
