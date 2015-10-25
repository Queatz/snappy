package com.queatz.snappy.team;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.util.Json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by jacob on 2/14/15.
 */
public class Things {
    public Team team;

    public Things(Team t) {
        team = t;
    }

    private void deepJson(Realm realm, RealmObject thing, JsonObject o) {
        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            String key = entry.getKey();

            Field field;

            try {
                field = thing.getClass().getSuperclass().getDeclaredField(key);
            }
            catch (NoSuchFieldException e) {
                //e.printStackTrace();
                //Log.w(Config.LOG_TAG, "JSON unknown field supplied: " + key);
                continue;
            }

            String fieldName = field.getName();
            Class fieldType = field.getType();
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            try {
                Method setter = thing.getClass().getDeclaredMethod(setterName, field.getType());

                JsonElement oo = o.get(fieldName);

                if(setter != null) {
                    if (oo.isJsonNull()) {
                        setter.invoke(thing, null);
                    } else if(RealmObject.class.isAssignableFrom(fieldType) && oo.isJsonObject()) {
                        setter.invoke(thing, put(realm, fieldType, oo.getAsJsonObject()));
                    } else if(RealmList.class.isAssignableFrom(fieldType) && oo.isJsonArray()) {
                        Class t = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                        setter.invoke(thing, putAll(realm, t, oo.getAsJsonArray()));
                    } else if(Date.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, Json.from(oo, Date.class));
                    } else if(String.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsString());
                    } else if(boolean.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsBoolean());
                    } else if(int.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsInt());
                    } else if(long.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsInt());
                    } else if(double.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsDouble());
                    } else if(float.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsFloat());
                    } else if(short.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsShort());
                    } else if(byte.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsByte());
                    } else if(char.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsCharacter());
                    } else if(Number.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, oo.getAsNumber());
                    } else {
                        try {
                            setter.invoke(thing, oo);
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Log.w(Config.LOG_TAG, "JSON setter not found for: " + fieldType.getName() + "." + fieldName);
                }
            }
            catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public <T extends RealmObject> T get() {
        return null;
    }

    public <T extends RealmObject> T get(Class<T> clazz, String id) {
        return team.realm.where(clazz).equalTo("id", id).findFirst();
    }

    public <T extends RealmObject> T put(Realm realm, Class<T> clazz, JsonObject jsonObject) {
        String localId = null, id;

        if (!jsonObject.has("id")) {
            Log.w(Config.LOG_TAG, "Object must have ID! " + jsonObject);
            return null;
        }

        if(jsonObject.has("localId")) {
            localId = jsonObject.get("localId").getAsString();
        }

        id = jsonObject.get("id").getAsString();

        T o;

        o = realm.where(clazz).equalTo("id", id).findFirst();

        if(o == null && localId != null) {
            o = realm.where(clazz).equalTo("id", localId).findFirst();
        }

        if(o == null)
            o = realm.createObject(clazz);

        deepJson(realm, o, jsonObject);

        return o;
    }

    public <T extends RealmObject> T put(Class<T> clazz, JsonObject jsonObject) {
        team.realm.beginTransaction();
        T o = put(team.realm, clazz, jsonObject);
        team.realm.commitTransaction();

        return o;
    }

    public <T extends RealmObject> T put(Class<T> clazz, String jsonObject) {
        if(jsonObject != null) {
            return put(clazz, Json.from(jsonObject, JsonObject.class));
        }
        else {
            return null;
        }
    }

    public <T extends RealmObject> RealmList<T> putAll(Class<T> clazz, JsonArray jsonArray) {
        team.realm.beginTransaction();
        RealmList<T> o = putAll(team.realm, clazz, jsonArray);
        team.realm.commitTransaction();

        return o;
    }

    public <T extends RealmObject> RealmList<T> putAll(Realm realm, Class<T> clazz, JsonArray jsonArray) {
        RealmList<T> results = new RealmList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            T o = put(realm, clazz, jsonArray.get(i).getAsJsonObject());
            results.add(o);
        }

        return results;
    }

    public <T extends RealmObject> RealmList<T> putAll(Class<T> clazz, String jsonArray) {
        if(jsonArray != null) {
            return putAll(clazz, Json.from(jsonArray, JsonArray.class));
        }
        else {
            return null;
        }
    }
}