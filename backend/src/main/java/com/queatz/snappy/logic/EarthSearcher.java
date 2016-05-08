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

    public List<Entity> getNearby(String kind, LatLng location, int count) {
        String queryString = "distance(geo, geopoint(" + location.latitude() + ", " + location.longitude() + ")) < " + Config.SEARCH_MAX_VISIBILITY;

        queryString += " AND kind = \"" + kind + "\"";

        SortOptions sortOptions = SortOptions.newBuilder().addSortExpression(
                SortExpression.newBuilder().setExpression("distance(geo, geopoint(" + location.latitude() + ", " + location.longitude() + "))")
                        .setDirection(SortExpression.SortDirection.ASCENDING).build()
        ).build();

        QueryOptions queryOptions = QueryOptions.newBuilder().setSortOptions(sortOptions).setLimit(count).build();
        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

        List<Entity> results = new ArrayList<>();

        for(ScoredDocument document : index.search(query)) {
            results.add(thingFromDocument(document));
        }

        return results;
    }

    private Entity thingFromDocument(Document document) {
        return EarthSingleton.of(EarthStore.class).get(document.getId());
    }

    private Document build(Entity object) {
        if (!object.contains(EarthField.GEO)) {
            return null;
        }

        Document.Builder builder = Document.newBuilder();

        LatLng latLng = object.getLatLng(EarthField.GEO);

        Field geoField = Field.newBuilder().setName(EarthField.GEO)
                .setGeoPoint(new GeoPoint(latLng.latitude(), latLng.longitude())).build();

        Field kindField = Field.newBuilder().setName(EarthField.KIND)
                .setAtom(object.getString(EarthField.KIND)).build();

        builder.addField(geoField);
        builder.addField(kindField);
        builder.setId(getId(object));

        return builder.build();
    }
}
