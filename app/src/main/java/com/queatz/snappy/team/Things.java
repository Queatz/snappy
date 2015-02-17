package com.queatz.snappy.team;

import android.util.Log;

import com.queatz.snappy.Config;
import com.queatz.snappy.things.Thing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.Iterator;

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

    private void deepJson(Realm realm, RealmObject thing, JSONObject o) {
        Iterator<String> keys = o.keys();

        while (keys.hasNext()) {
            String key = keys.next();

            Field field;

            try {
                field = thing.getClass().getSuperclass().getDeclaredField(key);
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
                Log.w(Config.LOG_TAG, "JSON unknown field supplied: " + key);
                continue;
            }

            String fieldName = field.getName();
            Class fieldType = field.getType();
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            try {
                Method setter = thing.getClass().getDeclaredMethod(setterName, field.getType());

                if(setter != null) {
                    if(RealmObject.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, team.things.put(realm, fieldType, o.getJSONObject(fieldName)));
                    }
                    else if(RealmList.class.isAssignableFrom(fieldType)) {
                        Class t = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                        setter.invoke(thing, team.things.putAll(realm, t, o.getJSONArray(fieldName)));
                    }
                    else if(Date.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, new Date(o.getString(fieldName)));
                    }
                    else if(boolean.class.isAssignableFrom(fieldType)) {
                        setter.invoke(thing, Boolean.valueOf(o.getString(fieldName)));
                    }
                    else {
                        setter.invoke(thing, o.get(fieldName));
                    }
                }
                else {
                    Log.w(Config.LOG_TAG, "JSON setter not found for: " + fieldType.getName() + "." + fieldName);
                }
            }
            catch (JSONException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public <T extends RealmObject & Thing> T get() {
        return null;
    }

    public <T extends RealmObject & Thing> T get(Class<T> clazz, String id) {
        return team.realm.where(clazz).equalTo("id", id).findFirst();
    }

    public <T extends RealmObject & Thing> T put(Realm realm, Class<T> clazz, JSONObject jsonObject) {
        String id;

        try {
            id = jsonObject.getString("id");
        }
        catch (JSONException e) {
            Log.w(Config.LOG_TAG, "Things *must* have an ID! thing = " + clazz.getName() + ", json = " + jsonObject);
            e.printStackTrace();
            return null;
        }

        T o = realm.where(clazz).equalTo("id", id).findFirst();

        if(o == null)
            o = realm.createObject(clazz);

        deepJson(realm, o, jsonObject);

        return o;
    }

    public <T extends RealmObject & Thing> T put(Class<T> clazz, JSONObject jsonObject) {
        team.realm.beginTransaction();
        T o = put(team.realm, clazz, jsonObject);
        team.realm.commitTransaction();

        return o;
    }

    public <T extends RealmObject & Thing> T put(Class<T> clazz, String jsonObject) {
        try {
            return put(clazz, new JSONObject(jsonObject));
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends RealmObject & Thing> RealmList<T> putAll(Class<T> clazz, JSONArray jsonArray) {
        team.realm.beginTransaction();
        RealmList<T> o = putAll(team.realm, clazz, jsonArray);
        team.realm.commitTransaction();

        return o;
    }

    public <T extends RealmObject & Thing> RealmList<T> putAll(Realm realm, Class<T> clazz, JSONArray jsonArray) {
        try {
            RealmList<T> results = new RealmList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                T o = put(realm, clazz, jsonArray.getJSONObject(i));
                results.add(o);
            }

            return results;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public <T extends RealmObject & Thing> RealmList<T> putAll(Class<T> clazz, String jsonArray) {
        try {
            return putAll(clazz, new JSONArray(jsonArray));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}