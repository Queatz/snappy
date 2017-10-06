package com.queatz.snappy.logic;

import com.google.common.collect.HashBiMap;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.logic.eventables.ChatEvent;
import com.queatz.snappy.logic.eventables.ClearNotificationEvent;
import com.queatz.snappy.logic.eventables.FollowEvent;
import com.queatz.snappy.logic.eventables.FormSubmissionEvent;
import com.queatz.snappy.logic.eventables.InformationEvent;
import com.queatz.snappy.logic.eventables.JoinAcceptedEvent;
import com.queatz.snappy.logic.eventables.JoinRequestEvent;
import com.queatz.snappy.logic.eventables.LikeEvent;
import com.queatz.snappy.logic.eventables.MessageEvent;
import com.queatz.snappy.logic.eventables.NewCommentEvent;
import com.queatz.snappy.logic.eventables.NewContactEvent;
import com.queatz.snappy.logic.eventables.NewOfferEvent;
import com.queatz.snappy.logic.eventables.NewPartyEvent;
import com.queatz.snappy.logic.eventables.NewThingEvent;
import com.queatz.snappy.logic.eventables.NewUpdateEvent;
import com.queatz.snappy.logic.eventables.OfferLikeEvent;
import com.queatz.snappy.logic.eventables.RefreshMeEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.service.Queue;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.shared.earth.EarthRef;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 6/19/16.
 */
public class EarthUpdate extends EarthControl {
    public EarthUpdate(EarthAs as) {
        super(as);
    }

    public static final Map<String, Class<? extends Eventable>> eventableMap = new HashMap<>();
    public static final Map<Class<? extends Eventable>, String> actionMap;

    static {
        eventableMap.put(Config.PUSH_ACTION_JOIN_ACCEPTED, JoinAcceptedEvent.class);
        eventableMap.put(Config.PUSH_ACTION_JOIN_REQUEST, JoinRequestEvent.class);
        eventableMap.put(Config.PUSH_ACTION_MESSAGE, MessageEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_PARTY, NewPartyEvent.class);
        eventableMap.put(Config.PUSH_ACTION_CLEAR_NOTIFICATION, ClearNotificationEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_UPTO, NewUpdateEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_OFFER, NewOfferEvent.class);
        eventableMap.put(Config.PUSH_ACTION_LIKE_UPDATE, LikeEvent.class);
        eventableMap.put(Config.PUSH_ACTION_OFFER_LIKED, OfferLikeEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_THING, NewThingEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_CONTACT, NewContactEvent.class);
        eventableMap.put(Config.PUSH_ACTION_FOLLOW, FollowEvent.class);
        eventableMap.put(Config.PUSH_ACTION_REFRESH_ME, RefreshMeEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_COMMENT, NewCommentEvent.class);
        eventableMap.put(Config.PUSH_ACTION_INFORMATION, InformationEvent.class);
        eventableMap.put(Config.PUSH_ACTION_FORM_SUBMISSION_EVENT, FormSubmissionEvent.class);
        eventableMap.put(Config.PUSH_ACTION_NEW_CHAT, ChatEvent.class);

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

        public EventableWrapper to(EarthRef key) {
            Queue.getService().enqueuePushMessageToUser(key.name(), action, event.toData());
            return this;
        }

        public EventableWrapper toFollowersOf(EarthRef key) {
            Queue.getService().enqueuePushMessageFromUser(key.name(), action, event.toData());
            return this;
        }

        public EventableWrapper to(EarthThing entity) {
            return to(entity.key());
        }

        public EventableWrapper toFollowersOf(EarthThing entity) {
            return toFollowersOf(entity.key());
        }

        public void toLocation(EarthGeo location) {
            Queue.getService().enqueuePushMessageFromLocation(location, action, event.toData());
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
        if (!actionMap.containsKey(event.getClass())) {
            throw new RuntimeException("Unknown push action for class : " + event.getClass().getSimpleName());
        }

        return new EventableWrapper(actionMap.get(event.getClass()), event);
    }
}
