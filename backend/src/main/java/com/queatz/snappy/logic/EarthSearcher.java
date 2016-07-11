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
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.queatz.snappy.shared.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 11/17/14.
 */
public class EarthSearcher {
    private Index index;

    public EarthSearcher() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName("Thing").build();
        index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    public boolean update(Entity object) {
        boolean success = updateGeo(object);
        updateLinked(object);
        return success;
    }

    private boolean updateGeo(Entity object) {
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

    public void delete(Entity object) {
        index.delete(getId(object));
    }

    public void delete(String id) {
        index.delete(id);
    }

    public String getId(Entity object) {
        return object.key().name();
    }

    public List<Entity> getNearby(String kind, String q, LatLng location, int count) {
        String queryString = "(";

        final String geoString = "geopoint(" + location.latitude() + ", " + location.longitude() + ")";

        queryString += "distance(geo, " + geoString + ") < " + Config.SEARCH_MAX_VISIBILITY;

        boolean searchWithLinks = true;

        if (searchWithLinks) {
            queryString += " OR distance(source_geo, " + geoString + ") < " + Config.SEARCH_MAX_VISIBILITY;
            queryString += " OR distance(target_geo, " + geoString + ") < " + Config.SEARCH_MAX_VISIBILITY;
        }

        queryString += ")";

        queryString += " AND kind = \"" + kind + "\"";

        if (q != null) {
            queryString += " AND name = \"" + q + "\"";
        }

        SortOptions sortOptions = SortOptions.newBuilder().addSortExpression(
                SortExpression.newBuilder().setExpression("distance(geo, geopoint(" + location.latitude() + ", " + location.longitude() + "))")
                        .setDirection(SortExpression.SortDirection.ASCENDING).build()
        ).build();

        QueryOptions queryOptions = QueryOptions.newBuilder().setSortOptions(sortOptions).setLimit(count).build();
        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

        List<Entity> results = new ArrayList<>();

        for(ScoredDocument document : index.search(query)) {
            Entity entity = thingFromDocument(document);

            if (entity != null) {
                results.add(entity);
            }
        }

        return results;
    }

    private Entity thingFromDocument(Document document) {
        final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
        return earthStore.get(document.getId());
    }

    private Document build(Entity object) {
        final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

        Document.Builder builder = Document.newBuilder();

        if (object.contains(EarthField.GEO)) {
            LatLng latLng = object.getLatLng(EarthField.GEO);

            Field geoField = Field.newBuilder().setName(EarthField.GEO)
                    .setGeoPoint(new GeoPoint(latLng.latitude(), latLng.longitude())).build();

            builder.addField(geoField);
        }

        Field kindField = Field.newBuilder().setName(EarthField.KIND)
                .setAtom(object.getString(EarthField.KIND)).build();

        builder.addField(kindField);
        builder.setId(getId(object));

        if (object.contains(EarthField.NAME) && object.getString(EarthField.NAME) != null) {
            Field nameField = Field.newBuilder().setName(EarthField.NAME)
                    .setText(tokenizeName(object.getString(EarthField.NAME))).build();

            builder.addField(nameField);
        }

        // Add linked geo's
        // Fields source, target, source_geo, target_geo
        for (String field : new String[] { EarthField.SOURCE, EarthField.TARGET }) {
            if (object.contains(field)) {
                Entity linkedEntity = earthStore.get(object.getKey(field));

                if (linkedEntity != null) {
                    Field linkedId = Field.newBuilder().setName(field)
                            .setAtom(object.key().name()).build();

                    builder.addField(linkedId);

                    if (linkedEntity.contains(EarthField.GEO)) {
                        LatLng linkedLatLng = linkedEntity.getLatLng(EarthField.GEO);

                        Field linkedGeo = Field.newBuilder().setName(field + "_" + EarthField.GEO)
                                .setGeoPoint(new GeoPoint(linkedLatLng.latitude(), linkedLatLng.longitude())).build();

                        builder.addField(linkedGeo);
                    }
                }
            }
        }

        return builder.build();
    }

    // XXX slow, and sluggish
    // Updates the linked locations of linked entities
    private void updateLinked(Entity entity) {
        final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

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
            for (int i = 1; i < token.length(); i++) {
                stringBuilder.append(token.substring(0, i));
            }
        }

        return stringBuilder.toString();
    }
}
