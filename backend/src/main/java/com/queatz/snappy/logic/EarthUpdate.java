package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.common.collect.HashBiMap;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.logic.eventables.ClearNotificationEvent;
import com.queatz.snappy.logic.eventables.JoinRequestEvent;
import com.queatz.snappy.logic.eventables.LikeEvent;
import com.queatz.snappy.logic.eventables.MessageEvent;
import com.queatz.snappy.logic.eventables.NewContactEvent;
import com.queatz.snappy.logic.eventables.NewOfferEvent;
import com.queatz.snappy.logic.eventables.NewPartyEvent;
import com.queatz.snappy.logic.eventables.NewThingEvent;
import com.queatz.snappy.logic.eventables.NewUpdateEvent;
import com.queatz.snappy.logic.eventables.OfferEndorsementEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.service.Queue;
import com.queatz.snappy.shared.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 6/19/16.
 */
public class EarthUpdate {

    public static final Map<String, Class<? extends Eventable>> eventableMap = new HashMap<>();
    public static final Map<Class<? extends Eventable>, String> actionMap;

    static {
        eventableMap.put(Config.PUSH_ACTION_JOIN_REQUEST, JoinRequestEvent.class);
        eventableMap.put(Config.PUSH_ACTION_MESSAGE, MessageEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_PARTY, NewPartyEvent.class);
        eventableMap.put(Config.PUSH_ACTION_CLEAR_NOTIFICATION, ClearNotificationEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_UPTO, NewUpdateEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_OFFER, NewOfferEvent.class);
        eventableMap.put(Config.PUSH_ACTION_LIKE_UPDATE, LikeEvent.class);
        eventableMap.put(Config.PUSH_ACTION_OFFER_ENDORSEMENT, OfferEndorsementEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_THING, NewThingEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_CONTACT, NewContactEvent.class);

        actionMap = HashBiMap.create(eventableMap).inverse();
    }

    /*
     * Used to map an event to the right recipients.
     */
    public static class EventableWrapper {
        final String action;
        final Eventable event;

        protected EventableWrapper(String action, Eventable event) {
            this.event = event;
            this.action = action;
        }

        public EventableWrapper to(Key key) {
            Queue.getService().enqueuePushMessageToUser(key.name(), action, event.toData());
            return this;
        }

        public EventableWrapper toFollowersOf(Key key) {
            Queue.getService().enqueuePushMessageFromUser(key.name(), action, event.toData());
            return this;
        }

        public EventableWrapper to(Entity entity) {
            return to(entity.key());
        }

        public EventableWrapper toFollowersOf(Entity entity) {
            return toFollowersOf(entity.key());
        }
    }

    public Eventable from(String action, String data) {
        if (!eventableMap.containsKey(action)) {
            throw new NothingLogicResponse("Unknown notification action: " + action);
        }

        try {
            Eventable eventable = eventableMap.get(action).getConstructor().newInstance();
            return (Eventable) eventableMap.get(action).getMethod("fromData", String.class).invoke(eventable, data);
        } catch (IllegalAccessException |
                InvocationTargetException |
                InstantiationException |
                NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public EventableWrapper send(Eventable event) {
        return new EventableWrapper(actionMap.get(event.getClass()), event);
    }
}
