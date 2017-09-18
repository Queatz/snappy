package com.queatz.snappy.chat;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.GeoIndexOptions;
import com.google.common.collect.ImmutableSet;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.shared.Gateway;
import com.queatz.snappy.shared.earth.EarthGeo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jacob on 8/10/17.
 */

public class ChatWorld {

    private static final String CHAT_COLLECTION = "Chat";
    private static ArangoDatabase __arangoDatabase;

    private final ArangoDatabase db;
    private final ArangoCollection collection;
    private final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true).waitForSync(true);

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
            __arangoDatabase.createCollection(CHAT_COLLECTION);
        } catch (ArangoDBException ignored) {
            // Whatever
        }

        __arangoDatabase.collection(CHAT_COLLECTION)
                .createGeoIndex(ImmutableSet.of(EarthField.GEO), new GeoIndexOptions());

        return __arangoDatabase;
    }

    public ChatWorld() {
        this.db = getDb();
        this.collection = db.collection(CHAT_COLLECTION);
    }


    public List<EarthThing> near(EarthGeo location) {
        String aql = "for x in near(" + CHAT_COLLECTION + ", @latitude, @longitude, @limit) sort x." + EarthField.CREATED_ON + " return x";

        Map<String, Object> vars = new HashMap<>();
        vars.put("latitude", location.getLatitude());
        vars.put("longitude", location.getLongitude());
        vars.put("limit", ChatConfig.MAX_CHAT_BACKFILL);

        ArangoCursor<BaseDocument> cursor = db.query(aql, vars, null, BaseDocument.class);

        List<EarthThing> result = new ArrayList<>();

        while (cursor.hasNext()) {
            result.add(EarthThing.from(cursor.next()));
        }

        return result;
    }

    public EarthThing.Builder stage(String kind) {
         return new EarthThing.Builder()
                .set(EarthField.CREATED_ON, new Date())
                .set(EarthField.KIND, kind);
    }

    public EarthThing add(EarthThing.Builder builder) {
        return new EarthThing(collection.insertDocument(builder.build(), options).getNew());
    }
}
