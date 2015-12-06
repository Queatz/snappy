package com.queatz.snappy.backend;

import com.google.appengine.api.datastore.GeoPt;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Deep;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.Shallow;
import com.queatz.snappy.shared.ThingSpec;
import com.queatz.snappy.shared.things.FollowLinkSpec;
import com.queatz.snappy.shared.things.JoinLinkSpec;
import com.queatz.snappy.shared.things.OfferSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.QuestLinkSpec;
import com.queatz.snappy.shared.things.QuestSpec;
import com.queatz.snappy.shared.things.UpdateLikeSpec;
import com.queatz.snappy.shared.things.UpdateSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jacob on 10/14/15.
 */
public class Json {
    static ExclusionStrategy hideExclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Hide.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    };

    static ExclusionStrategy shallowExclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Id.class) == null && fieldAttributes.getAnnotation(Shallow.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    };

    static ExclusionStrategy pushExclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Id.class) == null && fieldAttributes.getAnnotation(Push.class) == null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    };

    public enum Compression {
        PUSH,
        SHALLOW,
        NONE
    }

    static public <T> T from(final String json, final Class<T> clazz) {
        return new GsonBuilder().setDateFormat(DateFormat.LONG, DateFormat.LONG).create().fromJson(json, clazz);
    }

    static public String json(final Object thing) {
        return json(thing, Compression.NONE);
    }

    static public String json(final Object thing, final Compression compression) {
        // TODO this is terribly slow
        prepare(thing, compression == Compression.NONE ? 0 : 1);

        final GsonBuilder builder = new GsonBuilder()
                .setDateFormat(DateFormat.LONG, DateFormat.LONG)
                .addSerializationExclusionStrategy(hideExclusionStrategy);

        if (compression == Compression.SHALLOW) {
            builder.addSerializationExclusionStrategy(shallowExclusionStrategy);
        } else if (compression == Compression.PUSH) {
            builder.registerTypeAdapter(String.class, new StringClipper());
            builder.addSerializationExclusionStrategy(pushExclusionStrategy);
        } else {
            builder.addSerializationExclusionStrategy(shallowExclusionStrategy);

            Gson gson = builder.create();
            JsonElement jsonElement = builder.create().toJsonTree(thing);
            extend(gson, thing, jsonElement);

            return jsonElement.toString();
        }

        return builder.create().toJson(thing);
    }

    private static void extend(final Gson gson, final Object object, final JsonElement jsonElement) {
        if (jsonElement != null && object != null) {
            if (jsonElement.isJsonArray()) {
                for (int i = 0; i < jsonElement.getAsJsonArray().size(); i++) {
                    extend(gson, ((List) object).get(i), jsonElement.getAsJsonArray().get(i));
                }
            }

            if (!jsonElement.isJsonObject()) {
                return;
            }

            for (Field field : shallowFields(object.getClass())) {
                try {
                    jsonElement.getAsJsonObject().add(field.getName(), gson.toJsonTree(field.get(object)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            for (Field field : deepFields(object.getClass())) {
                try {
                    extend(gson, field.get(object),
                            jsonElement.getAsJsonObject().has(field.getName()) ?
                                    jsonElement.getAsJsonObject().get(field.getName()) :
                                    gson.toJsonTree(field.get(object))
                    );
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final Map<Class, List<Field>> shallow_fields = new HashMap<>();
    private static final Map<Class, List<Field>> deep_fields = new HashMap<>();

    private static List<Field> shallowFields(Class type) {
        if (!shallow_fields.containsKey(type)) {
            List<Field> fields = new ArrayList<>();

            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Shallow.class)) {
                    fields.add(field);
                }
            }

            shallow_fields.put(type, fields);
        }

        return shallow_fields.get(type);
    }

    private static List<Field> deepFields(Class type) {
        if (!deep_fields.containsKey(type)) {
            List<Field> fields = new ArrayList<>();

            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Deep.class)) {
                    fields.add(field);
                }
            }

            deep_fields.put(type, fields);
        }

        return deep_fields.get(type);
    }

    /* Prepare Things */

    static private void prepare(Object object, int depth) {
        if (object == null) {
            return;
        }

        if (List.class.isAssignableFrom(object.getClass())) {
            for (Object o : (List) object) {
                prepare(o, depth);
            }
            return;
        }

        if (Map.class.isAssignableFrom(object.getClass())) {
            for (Object o : ((Map) object).values()) {
                prepare(o, depth);
            }
            return;
        }

        specialPrepare(object);

        for(Field field : getListFields(object.getClass())) {
            if (depth > 0 && field.isAnnotationPresent(Shallow.class)) {
                continue;
            }

            try {
                List list = (List) field.get(object);

                if (list != null) {
                    for (Object o : list) {
                        prepare(o, depth + 1 - fieldDepth(field));
                    }
                }
            } catch (IllegalAccessException e) {
                Log.log(Level.WARNING, "Internal problem #193370", e);
            }
        }

        for(Field field : getMapFields(object.getClass())) {
            if (depth > 0 && field.isAnnotationPresent(Shallow.class)) {
                continue;
            }

            try {
                Map map = (Map) field.get(object);

                if (map != null) {
                    for (Object o : map.values()) {
                        prepare(o, depth + 1 - fieldDepth(field));
                    }
                }
            } catch (IllegalAccessException e) {
                Log.log(Level.WARNING, "Internal problem #193373", e);
            }
        }

        GeoPtFields geoPtFields = getGeoPtField(object.getClass());

        if (geoPtFields != null) {
            try {
                GeoPt geoPt = (GeoPt) geoPtFields.latlng.get(object);

                if (geoPt != null) {
                    geoPtFields.latitude.set(object, geoPt.getLatitude());
                    geoPtFields.longitude.set(object, geoPt.getLongitude());
                }
            } catch (IllegalAccessException e) {
                Log.log(Level.WARNING, "Internal problem #193371", e);
            }
        }

        for (ResolveFields field : getFields(object.getClass())) {
            if (depth > 0 && field.thing.isAnnotationPresent(Shallow.class)) {
                continue;
            }

            try {
                Key<? extends ThingSpec> key = (Key<? extends ThingSpec>) field.id.get(object);

                if (key != null) {
                    field.thing.set(object, Datastore.get(key));
                }
            } catch (IllegalAccessException e) {
                Log.log(Level.WARNING, "Internal problem #193372", e);
            }
        }

        for (Field field : getAllFields(object.getClass())) {
            try {
                prepare(field.get(object), depth + 1 - fieldDepth(field));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: Special Prepare
    // TODO: disband this with rulesets

    private static void specialPrepare(Object object) {
        if (QuestSpec.class.isAssignableFrom(object.getClass())) {
            QuestSpec quest = (QuestSpec) object;

            if (quest.team == null) {
                quest.team = new ArrayList<>();

                for (QuestLinkSpec link : Datastore.get(QuestLinkSpec.class).filter("questId", quest).list()) {
                    quest.team.add(Datastore.get(link.personId));
                }
            }
        }

        else if (PersonSpec.class.isAssignableFrom(object.getClass())) {
            PersonSpec person = (PersonSpec) object;

            if (person.updates == null) {
                person.updates = new ArrayList<>();

                for (UpdateSpec update : Datastore.get(UpdateSpec.class).filter("personId", person).order("-date").limit(Config.SEARCH_MAXIMUM).list()) {
                    person.updates.add(update);
                }
            }

            if (person.offers == null) {
                person.offers = new ArrayList<>();

                for (OfferSpec offer : Datastore.get(OfferSpec.class).filter("personId", person).list()) {
                    person.offers.add(offer);
                }
            }

            if (person.infoFollowers <= 0) {
                person.infoFollowers = Datastore.get(FollowLinkSpec.class).filter("targetId", person).count();
            }

            if (person.infoFollowing <= 0) {
                person.infoFollowing = Datastore.get(FollowLinkSpec.class).filter("sourceId", person).count();
            }

            if (person.infoHosted <= 0) {
                person.infoHosted = Datastore.get(PartySpec.class).filter("hostId", person).count();
            }
        }

        else if (PartySpec.class.isAssignableFrom(object.getClass())) {
            PartySpec party = (PartySpec) object;

            if (party.people == null) {
                party.people = new ArrayList<>();

                for (JoinLinkSpec join : Datastore.get(JoinLinkSpec.class).filter("partyId", party).list()) {
                    party.people.add(join);
                }
            }
        }

        else if (UpdateSpec.class.isAssignableFrom(object.getClass())) {
            UpdateSpec update = (UpdateSpec) object;

            if (Config.UPDATE_ACTION_UPTO.equals(update.action)) {
                update.likers = Datastore.get(UpdateLikeSpec.class).filter("targetId", update).count();
            }
        }
    }

    private static int fieldDepth(Field field) {
        return field.isAnnotationPresent(Deep.class) ? field.getAnnotation(Deep.class).value() : 0;
    }

    private static Logger Log = Logger.getLogger(Config.NAME);

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

    private static final Map<Class, List<ResolveFields>> fields = new HashMap<>();
    private static final Map<Class, GeoPtFields> geopt_fields = new HashMap<>();
    private static final Map<Class, List<Field>> list_fields = new HashMap<>();
    private static final Map<Class, List<Field>> map_fields = new HashMap<>();
    private static final Map<Class, List<Field>> all_fields = new HashMap<>();

    private static List<Field> getListFields(Class type) {
        if (!list_fields.containsKey(type)) {
            ArrayList<Field> resolve = new ArrayList<>();

            for (Field field : type.getDeclaredFields()) {
                if (List.class.isAssignableFrom(field.getType())) {
                    resolve.add(field);
                }
            }

            list_fields.put(type, resolve);
        }

        return list_fields.get(type);
    }

    private static List<Field> getMapFields(Class type) {
        if (!map_fields.containsKey(type)) {
            ArrayList<Field> resolve = new ArrayList<>();

            for (Field field : type.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(field.getType())) {
                    resolve.add(field);
                }
            }

            map_fields.put(type, resolve);
        }

        return map_fields.get(type);
    }

    private static GeoPtFields getGeoPtField(Class type) {
        if (!geopt_fields.containsKey(type)) {
            try {
                geopt_fields.put(type, new GeoPtFields(
                        type.getDeclaredField("latlng"),
                        type.getDeclaredField("latitude"),
                        type.getDeclaredField("longitude")
                ));

            } catch (NoSuchFieldException ignored) {
                geopt_fields.put(type, null);
            }
        }

        return geopt_fields.get(type);
    }

    private static List<ResolveFields> getFields(Class type) {
        if (!fields.containsKey(type)) {
            ArrayList<ResolveFields> resolve = new ArrayList<>();

            for (Field field : type.getDeclaredFields()) {
                try {
                    if (ThingSpec.class.isAssignableFrom(field.getType())) {
                        resolve.add(new ResolveFields(field, type.getDeclaredField(field.getName() + "Id")));
                    }
                } catch (NoSuchFieldException ignored) {
                    Log.log(Level.WARNING, "No matching Id field for: " + type);
                }
            }

            fields.put(type, resolve);
        }

        return fields.get(type);
    }

    private static List<Field> getAllFields(Class type) {
        if (!all_fields.containsKey(type)) {
            ArrayList<Field> resolve = new ArrayList<>();

            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) ||
                        !Modifier.isPublic(field.getModifiers()) ||
                        List.class.isAssignableFrom(field.getType()) ||
                        Map.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                resolve.add(field);
            }

            all_fields.put(type, resolve);
        }

        return all_fields.get(type);
    }
}
