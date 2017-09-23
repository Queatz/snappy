package com.queatz.snappy.logic;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.shared.Gateway;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by jacob on 9/22/17.
 */

public class AppStore {

    private static final String APP_STORE_TOKEN_COLLECTION = "AppStoreToken";
    private static ArangoDatabase __arangoDatabase;

    private final ArangoDatabase db;
    private final ArangoCollection collection;
    private final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true).waitForSync(true);
    private final EarthAs as;

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
            __arangoDatabase.createCollection(APP_STORE_TOKEN_COLLECTION);
        } catch (ArangoDBException ignored) {
            // Whatever
        }

        return __arangoDatabase;
    }

    public AppStore(EarthAs as) {
        this.as = as;
        this.db = getDb();
        this.collection = db.collection(APP_STORE_TOKEN_COLLECTION);
    }

    @NotNull
    public String tokenForDomain(@NotNull String domain) {
        as.requireUser();

        String aql = "for x in " + APP_STORE_TOKEN_COLLECTION + " filter x." + AppStoreField.DOMAIN + " == @domain and x." + AppStoreField.USER + " == @user limit 1 return x." + AppStoreField.TOKEN;

        ArangoCursor<String> cursor = db.query(aql, ImmutableMap.of(
                "domain", domain,
                "user", as.getUser().key().name()
        ), null, String.class);

        if (cursor.hasNext()) {
            return cursor.next();
        }

        BaseDocument token = new BaseDocument();
        token.addAttribute(AppStoreField.DOMAIN, domain);
        token.addAttribute(AppStoreField.USER, as.getUser().key().name());
        token.addAttribute(AppStoreField.TOKEN, Util.randomToken());

        token = collection.insertDocument(token, options).getNew();

        return (String) token.getAttribute(AppStoreField.TOKEN);
    }

    @Nullable
    public BaseDocument appForToken(@NotNull String token) {
        String aql = "for x in " + APP_STORE_TOKEN_COLLECTION + " filter x." + AppStoreField.TOKEN + " == @token limit 1 return x";

        ArangoCursor<BaseDocument> cursor = db.query(aql, ImmutableMap.of(
                "token", token
        ), null, BaseDocument.class);

        if (cursor.hasNext()) {
            return cursor.next();
        }

        return null;
    }
}
