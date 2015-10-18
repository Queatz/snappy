package com.queatz.snappy.service;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.search.Document;
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
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.ThingSpec;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jacob on 11/17/14.
 */
public class Search {
    private static Search _service;

    public static Search getService() {
        if(_service == null)
            _service = new Search();

        return _service;
    }

    private static HashMap<Class, Map<String, Field>> _fieldsCache = new HashMap<>();

    private Index index;

    public Search() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName("local").build();
        index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    public <T extends ThingSpec> boolean update(T object) {
        try {
            index.put(build(object));
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public <T extends ThingSpec> List<T> getNearby(Class<T> type, GeoPt location, Date age, int count) {
        return getNearby(type, location, age, null, count);
    }

    public <T extends ThingSpec> List<T> getNearby(Class<T> type, GeoPt location, Date age, String string, int count) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try {
            String queryString = "distance(geo, geopoint(" + location.getLatitude() + ", " + location.getLatitude() + ")) < " + Config.SEARCH_MAX_VISIBILITY;

            if (age != null) {
                queryString += " AND age >= " + format.format(age);
            }

            if (string != null) {
                queryString += " " + string;
            }

            SortOptions sortOptions = SortOptions.newBuilder().addSortExpression(
                    SortExpression.newBuilder().setExpression("distance(geo, geopoint(" + location.getLatitude() + ", " + location.getLongitude() + "))")
                            .setDirection(SortExpression.SortDirection.ASCENDING).build()
            ).build();

            QueryOptions queryOptions = QueryOptions.newBuilder().setSortOptions(sortOptions).setLimit(count).build();
            Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

            ArrayList<T> results = new ArrayList<>();

            for(ScoredDocument document : index.search(query)) {
                results.add(typeFromDocument(type, document));
            }

            return results;
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T extends ThingSpec> T typeFromDocument(Class<T> type, Document document) {
        String id = document.getOnlyField("id").getAtom();

        return Datastore.get(type).id(id).now();
    }

    private <T extends ThingSpec> Document build(T object) {
        Document.Builder builder = Document.newBuilder();

        try {
            Map<String, Field> fields = getSearchFields(object.getClass());

            if (fields.size() < 1) {
                return null;
            }

            for (String search : fields.keySet()) {
                Field field = fields.get(search);

                com.google.appengine.api.search.Field.Builder fieldBuilder =
                        com.google.appengine.api.search.Field.newBuilder().setName(search);

                if (GeoPt.class.isAssignableFrom(field.getType())) {
                    GeoPt geoPt = (GeoPt) fields.get(search).get(object);

                    if (geoPt != null) {
                        fieldBuilder.setGeoPoint(new GeoPoint(geoPt.getLatitude(), geoPt.getLongitude()));
                    }
                } else if (Date.class.isAssignableFrom(field.getType())) {
                    Date date = (Date) fields.get(search).get(object);

                    if (date != null) {
                        fieldBuilder.setDate(date);
                    }
                } else if (String.class.isAssignableFrom(field.getType())) {
                    String string = (String) fields.get(search).get(object);

                    if (string != null) {
                        fieldBuilder.setText(string);
                    }
                } else if (Number.class.isAssignableFrom(field.getType())) {
                    Number number = (Number) fields.get(search).get(object);

                    if (number != null) {
                        fieldBuilder.setNumber(number.doubleValue());
                    }
                }

                builder.addField(fieldBuilder.build());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        builder.setId(object.getClass().getSimpleName() + "-" + object.id);

        com.google.appengine.api.search.Field f;

        f = com.google.appengine.api.search.Field.newBuilder()
                .setName("id")
                .setAtom(object.id)
                .build();

        builder.addField(f);

        f = com.google.appengine.api.search.Field.newBuilder()
                .setName("type")
                .setAtom(object.getClass().getSimpleName())
                .build();

        builder.addField(f);

        return builder.build();
    }

    private Map<String, Field> getSearchFields(Class clz) {
        if (_fieldsCache.containsKey(clz)) {
            return _fieldsCache.get(clz);
        }

        HashMap<String, Field> fields = new HashMap<>();

        for (Field field : clz.getDeclaredFields()) {
            if (field.isAnnotationPresent(com.queatz.snappy.shared.Search.class)) {
                fields.put(field.getAnnotation(com.queatz.snappy.shared.Search.class).value(), field);
            }
        }

        _fieldsCache.put(clz, fields);
        return fields;
    }
}
