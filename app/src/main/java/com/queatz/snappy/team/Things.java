package com.queatz.snappy.team;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.util.Json;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by jacob on 2/14/15.
 *
 * Do not use this class across multiple threads.
 */
public class Things {
    private DynamicRealm realm;

    private final Map<String, DynamicRealmObject> addedInTransaction = new HashMap<>();

    public Things(DynamicRealm realm) {
        this.realm = realm;
    }

    private void deepJson(DynamicRealm realm, DynamicRealmObject thing, JsonObject o) {
        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {

            switch (entry.getKey()) {
                // String
                case Thing.ID:
                case Thing.NAME:
                case Thing.ABOUT:
                case Thing.KIND:
                case Thing.ADDRESS:
                case Thing.UNIT:
                case Thing.STATUS:
                case Thing.ACTION:
                case Thing.FIRST_NAME:
                case Thing.LAST_NAME:
                case Thing.IMAGE_URL:
                case Thing.AUTH:
                case Thing.GOOGLE_URL:
                case Thing.SOCIAL_MODE:
                case Thing.MESSAGE:
                case Thing.PLACEHOLDER:
                case Thing.ROLE:
                    thing.setString(entry.getKey(), entry.getValue().getAsString());
                    break;

                // Object
                case Thing.LOCATION:
                case Thing.SOURCE:
                case Thing.TARGET:
                case Thing.LATEST:
                case Thing.HOST:
                case Thing.FROM:
                case Thing.TO:
                    thing.setObject(entry.getKey(), put(realm, entry.getValue().getAsJsonObject()));
                    break;

                // Date
                case Thing.CREATED_ON:
                case Thing.INFO_UPDATED:
                case Thing.UPDATED:
                case Thing.DATE:
                    thing.setDate(entry.getKey(), Json.from(entry.getValue(), Date.class));
                    break;

                // Boolean
                case Thing.PHOTO:
                case Thing.SEEN:
                case Thing.FULL:
                case Thing.WANT:
                case Thing.OWNER:
                case Thing.HIDDEN:
                case Thing.GOING:
                    thing.setBoolean(entry.getKey(), entry.getValue().getAsBoolean());
                    break;

                // Double
                case Thing.LATITUDE:
                case Thing.LONGITUDE:
                case Thing.INFO_DISTANCE:
                case Thing.ASPECT:
                    thing.setDouble(entry.getKey(), entry.getValue().getAsDouble());
                    break;

                // Special (GEO only)
                case Thing.GEO:
                    JsonObject geo = entry.getValue().getAsJsonObject();
                    thing.setDouble(Thing.LATITUDE, geo.get(Thing.LATITUDE).getAsDouble());
                    thing.setDouble(Thing.LONGITUDE, geo.get(Thing.LONGITUDE).getAsDouble());

                    break;

                // Integer
                case Thing.PRICE:
                case Thing.LIKERS:
                case Thing.INFO_OFFERS:
                case Thing.INFO_FOLLOWERS:
                case Thing.BACKERS:
                case Thing.INFO_FOLLOWING:
                    thing.setInt(entry.getKey(), entry.getValue().getAsInt());
                    break;

                // List
                case Thing.MEMBERS:
                case Thing.IN:
                case Thing.CLUBS:
                case Thing.JOINS:
                    thing.setList(entry.getKey(), putAll(realm, entry.getValue().getAsJsonArray()));
                    break;

                default:
                    Log.w(Config.LOG_TAG, "Unknown value supplied: " + entry.getKey());
            }
        }
    }

    public DynamicRealmObject get() {
        return null;
    }

    public DynamicRealmObject get(@NonNull String id) {
        return realm.where("Thing").equalTo(Thing.ID, id).findFirst();
    }

    public DynamicRealmObject put(DynamicRealm realm, JsonObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        String localId = null, id;

        if (!jsonObject.has(Thing.ID)) {
            Log.w(Config.LOG_TAG, "Object must have ID! " + jsonObject);
            return null;
        }

        if(jsonObject.has("localId")) {
            localId = jsonObject.get("localId").getAsString();
        }

        id = jsonObject.get(Thing.ID).getAsString();

        DynamicRealmObject o = null;

        if (addedInTransaction.containsKey(id)) {
            o = addedInTransaction.get(id);
        }

        if (o == null) {
            o = realm.where("Thing")
                    .equalTo(Thing.ID, id)
                    .findFirst();

            if (o != null) addedInTransaction.put(id, o);
        }

        if(o == null && localId != null) {
            o = realm.where("Thing")
                    .equalTo(Thing.ID, localId)
                    .findFirst();

            if (o != null) addedInTransaction.put(id, o);
        }

        if(o == null) {
            o = realm.createObject("Thing");

            if (o != null) addedInTransaction.put(id, o);
        }

        deepJson(realm, o, jsonObject);

        return o;
    }

    public DynamicRealmObject put(JsonObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        realm.beginTransaction();
        DynamicRealmObject o = put(realm, jsonObject);
        realm.commitTransaction();
        addedInTransaction.clear();

        return o;
    }

    public DynamicRealmObject put(String jsonObject) {
        if(jsonObject != null) {
            return put(Json.from(jsonObject, JsonObject.class));
        }
        else {
            return null;
        }
    }

    public RealmList<DynamicRealmObject> putAll(JsonArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }
        realm.beginTransaction();
        RealmList<DynamicRealmObject> o = putAll(realm, jsonArray);
        realm.commitTransaction();
        addedInTransaction.clear();

        return o;
    }

    public RealmList<DynamicRealmObject> putAll(DynamicRealm realm, JsonArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }

        RealmList<DynamicRealmObject> results = new RealmList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            DynamicRealmObject o = put(realm, jsonArray.get(i).getAsJsonObject());
            results.add(o);
        }

        return results;
    }

    public RealmList<DynamicRealmObject> putAll(String jsonArray) {
        if(jsonArray == null) {
            return null;
        }

        return putAll(Json.from(jsonArray, JsonArray.class));
    }

    /**
     * Locally deletes remotely deleted items by comparing two lists together.
     *
     * @param current The current know list of items.
     * @param actual The refreshed list from the server. Must be the complete list.
     */
    public <T extends RealmObject> void diff(List<T> current, List<T> actual) {
        if (current == null || actual == null) {
            return;
        }

        realm.beginTransaction();

        for (int i = 0; i < current.size(); i++) {
            if (!actual.contains(current.get(i))) {
                current.get(i).deleteFromRealm();
            }
        }

        realm.commitTransaction();
    }
}