package com.queatz.snappy.backend;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.LoadType;
import com.googlecode.objectify.cmd.Query;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.shared.ThingSpec;
import com.queatz.snappy.shared.things.BountySpec;
import com.queatz.snappy.shared.things.ContactSpec;
import com.queatz.snappy.shared.things.FollowLinkSpec;
import com.queatz.snappy.shared.things.GroupSpec;
import com.queatz.snappy.shared.things.JoinLinkSpec;
import com.queatz.snappy.shared.things.LocationSpec;
import com.queatz.snappy.shared.things.MessageSpec;
import com.queatz.snappy.shared.things.OfferSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.QuestLinkSpec;
import com.queatz.snappy.shared.things.QuestSpec;
import com.queatz.snappy.shared.things.UpdateLikeSpec;
import com.queatz.snappy.shared.things.UpdateSpec;

import java.lang.reflect.ParameterizedType;
import java.util.Random;

public class Datastore {

    static {
        ObjectifyService.register(RegistrationRecord.class);
        ObjectifyService.register(GooglePurchaseDataSpec.class);

        ObjectifyService.register(BountySpec.class);
        ObjectifyService.register(ContactSpec.class);
        ObjectifyService.register(FollowLinkSpec.class);
        ObjectifyService.register(GroupSpec.class);
        ObjectifyService.register(JoinLinkSpec.class);
        ObjectifyService.register(LocationSpec.class);
        ObjectifyService.register(MessageSpec.class);
        ObjectifyService.register(OfferSpec.class);
        ObjectifyService.register(PartySpec.class);
        ObjectifyService.register(PersonSpec.class);
        ObjectifyService.register(QuestLinkSpec.class);
        ObjectifyService.register(QuestSpec.class);
        ObjectifyService.register(UpdateLikeSpec.class);
        ObjectifyService.register(UpdateSpec.class);
    }

    public static <T extends ThingSpec> T create(Class<T> type) {
        try {
            T thing = type.newInstance();
            thing.id = newId();
            return thing;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String newId() {
        return Long.toString(new Random().nextLong());
    }

    public static String id(Key key) {
        return key.getString();
    }

    public static <T> LoadType<T> get(Class<T> clazz) {
        return ofy().load().type(clazz);
    }

    public static <T> Query<T> get(Class<T> clazz, com.google.appengine.api.datastore.Query.Filter filter) {
        return ofy().load().type(clazz).filter(filter);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Key<T> key) {
        return (T) get(typeFromKey(key), key.getString());
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> typeFromKey(Key key) {
        return (Class<T>) ((ParameterizedType) key.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public static <T> T get(Class<T> clazz, String id) {
        return ofy().load().type(clazz).id(id).now();
    }

    public static <T> boolean save(T thing) {
        if (ThingSpec.class.isAssignableFrom(thing.getClass())) {
            if (((ThingSpec) thing).id == null) {
                ((ThingSpec) thing).id = newId();
            }

            Search.getService().update((ThingSpec) thing);
        }

        return ofy().save().entity(thing).now() != null;

    }

    public static <T> void delete(T thing) {
        ofy().delete().entity(thing).now();
    }

    public static <T> void delete(Class<T> type, String id) {
        ofy().delete().type(type).id(id).now();
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

    public static <T> Key<T> key(T thing) {
        return Key.create(thing);
    }

    public static <T> Key<T> key(Class<T> type, String id) {
        return Key.create(type, id);
    }
}
