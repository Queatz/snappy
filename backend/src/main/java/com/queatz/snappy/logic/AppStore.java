package com.queatz.snappy.logic;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.appstore.AppStoreField;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Gateway;
import com.queatz.snappy.shared.Shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 9/22/17.
 */

public class AppStore {

    private static final String APP_STORE_TOKEN_COLLECTION = "AppStoreToken";
    private static final String APP_STORE_VALUES_COLLECTION = "AppStoreValues";
    private static ArangoDatabase __arangoDatabase;

    private final ArangoDatabase db;
    private final ArangoCollection collection;
    private final ArangoCollection valuesCollection;
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
            __arangoDatabase.createCollection(APP_STORE_VALUES_COLLECTION);
        } catch (ArangoDBException ignored) {
            // Whatever
        }

        return __arangoDatabase;
    }

    public AppStore(EarthAs as) {
        this.as = as;
        this.db = getDb();
        this.collection = db.collection(APP_STORE_TOKEN_COLLECTION);
        this.valuesCollection = db.collection(APP_STORE_VALUES_COLLECTION);
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
        token.addAttribute(AppStoreField.TOKEN, Shared.randomToken());

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

    public String get(String appToken, String q) {
        BaseDocument document = getDocument(appForToken(appToken), q);

        if (document == null) {
            as.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return (String) document.getAttribute(AppStoreField.DATA);
    }

    public String put(String appToken, String q, String v) {
        BaseDocument app = appForToken(appToken);

        if (app == null) {
            throw new NothingLogicResponse("app - no");
        }

        BaseDocument document = getDocument(app, q);

        boolean success = false;

        if (document == null) {
            document = new BaseDocument();
            document.addAttribute(AppStoreField.DOMAIN, app.getAttribute(AppStoreField.DOMAIN));
            document.addAttribute(AppStoreField.USER, app.getAttribute(AppStoreField.USER));
            document.addAttribute(AppStoreField.NAME, q);
            document.addAttribute(AppStoreField.DATA, v);
            success = valuesCollection.insertDocument(document, options).getNew() != null;
        } else {
            document.updateAttribute(AppStoreField.DATA, v);
            success = valuesCollection.updateDocument(document.getKey(), document).getKey() != null;
        }

        return new SuccessView(success).toJson();
    }

    @Nullable
    private BaseDocument getDocument(BaseDocument app, String q) {
        if (app == null) {
            return null;
        }

        String aql = "for x in " + APP_STORE_VALUES_COLLECTION + " filter x." + AppStoreField.DOMAIN + " == @domain and x." + AppStoreField.USER + " == @user and x." + AppStoreField.NAME + " == @name limit 1 return x";

        ArangoCursor<BaseDocument> cursor = db.query(aql, ImmutableMap.of(
                "domain", (String) app.getAttribute(AppStoreField.DOMAIN),
                "user", (String) app.getAttribute(AppStoreField.USER),
                "name", q
        ), null, BaseDocument.class);

        if (cursor.hasNext()) {
            return cursor.next();
        }

        return null;
    }
}
