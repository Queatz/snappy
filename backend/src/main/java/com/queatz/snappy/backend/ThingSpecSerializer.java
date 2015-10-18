package com.queatz.snappy.backend;

import com.google.appengine.api.datastore.GeoPt;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.googlecode.objectify.annotation.Ignore;
import com.queatz.snappy.shared.ThingSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jacob on 10/16/15.
 *
 * Ensures all references are loaded
 */
public class ThingSpecSerializer implements JsonSerializer<ThingSpec> {
    private static Logger Log = Logger.getLogger(ThingSpecSerializer.class.getName());

    private static class ResolveFields {
        Field thing;
        Field id;

        public ResolveFields(Field thing, Field id) {
            this.thing = thing;
            this.id = id;
        }
    }

    private static class GeoPtFields {
        Field latlng;
        Field latitude;
        Field longitude;

        public GeoPtFields(Field latlng, Field latitude, Field longitude) {
            this.latlng = latlng;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private static final Map<Class<? extends ThingSpec>, List<ResolveFields>> fields = new HashMap<>();
    private static final Map<Class<? extends ThingSpec>, GeoPtFields> geopt_fields = new HashMap<>();

    @SuppressWarnings("unchecked")
    public JsonElement serialize(final ThingSpec thing, final Type type, final JsonSerializationContext context) {
        GeoPtFields geoPtFields = getGeoPtField(thing.getClass());

        if (geoPtFields != null) {
            try {
                GeoPt geoPt = (GeoPt) geoPtFields.latlng.get(thing);
                geoPtFields.latitude.set(thing, geoPt.getLatitude());
                geoPtFields.longitude.set(thing, geoPt.getLongitude());
            } catch (IllegalAccessException e) {
                Log.log(Level.WARNING, "Internal problem #193371", e);
            }
        }

        for (ResolveFields field : getFields(thing.getClass())) {
            try {
                Object resolve = field.thing.get(thing);

                if (resolve == null) {
                    field.thing.set(thing, load((Class<? extends ThingSpec>) field.thing.getType(), (String) field.id.get(thing)));
                }
            } catch (IllegalAccessException e) {
                Log.log(Level.WARNING, "Internal problem #193372", e);
            }
        }

        return context.serialize(thing);
    }

    private ThingSpec load(Class<? extends ThingSpec> thingType, String id) {
        return Datastore.get(thingType, id);
    }

    private static List<ResolveFields> getFields(Class<? extends ThingSpec> type) {
        if (!fields.containsKey(type)) {
            ArrayList<ResolveFields> resolve = new ArrayList<>();

            for (Field field : type.getDeclaredFields()) {
                try {
                    if (field.isAnnotationPresent(Ignore.class) && ThingSpec.class.isAssignableFrom(field.getType())) {
                        resolve.add(new ResolveFields(field, type.getField(field.getName() + "Id")));
                    }
                } catch (NoSuchFieldException ignored) {
                    Log.log(Level.WARNING, "No matching Id field for: " + type);
                }
            }

            fields.put(type, resolve);
        }

        return fields.get(type);
    }

    private static GeoPtFields getGeoPtField(Class<? extends ThingSpec> type) {
        if (!geopt_fields.containsKey(type)) {
            try {
                geopt_fields.put(type, new GeoPtFields(
                        type.getField("latlng"),
                        type.getField("latitude"),
                        type.getField("longitude")
                ));

            } catch (NoSuchFieldException ignored) {
                Log.log(Level.WARNING, "No matching Id field for: " + type);
                geopt_fields.put(type, null);
            }
        }

        return geopt_fields.get(type);
    }
}
