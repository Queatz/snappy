package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.logic.eventables.ClearNotificationEvent;
import com.queatz.snappy.logic.eventables.FollowEvent;
import com.queatz.snappy.logic.eventables.JoinRequestEvent;
import com.queatz.snappy.logic.eventables.LikeEvent;
import com.queatz.snappy.logic.eventables.MessageEvent;
import com.queatz.snappy.logic.eventables.NewOfferEvent;
import com.queatz.snappy.logic.eventables.NewPartyEvent;
import com.queatz.snappy.logic.eventables.NewUpdateEvent;
import com.queatz.snappy.logic.eventables.OfferEndorsementEvent;
import com.queatz.snappy.logic.eventables.RefreshMeEvent;
import com.queatz.snappy.logic.exceptions.LogicException;
import com.queatz.snappy.shared.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 6/19/16.
 */
public class EarthUpdate {
    /**
     * Maps event actions to their right notification generators.
     */
    private static final Map<String, Eventable> eventableMap = new HashMap<>();

    static {
        eventableMap.put(Config.PUSH_ACTION_JOIN_REQUEST, new JoinRequestEvent());
        eventableMap.put(Config.PUSH_ACTION_FOLLOW, new FollowEvent());
        eventableMap.put(Config.PUSH_ACTION_MESSAGE, new MessageEvent());
        eventableMap.put(Config.PUSH_ACTION_NEW_PARTY, new NewPartyEvent());
        eventableMap.put(Config.PUSH_ACTION_REFRESH_ME, new RefreshMeEvent());
        eventableMap.put(Config.PUSH_ACTION_CLEAR_NOTIFICATION, new ClearNotificationEvent());
        eventableMap.put(Config.PUSH_ACTION_NEW_UPTO, new NewUpdateEvent());
        eventableMap.put(Config.PUSH_ACTION_NEW_OFFER, new NewOfferEvent());
        eventableMap.put(Config.PUSH_ACTION_LIKE_UPDATE, new LikeEvent());
        eventableMap.put(Config.PUSH_ACTION_OFFER_ENDORSEMENT, new OfferEndorsementEvent());
    }

    /**
     * Add an event.
     *
     * Lets the right people know about the event that happened.
     *
     * @param event The kind of event to create.
     */
    public Eventable get(String event) {
        if (!eventableMap.containsKey(event)) {
            throw new LogicException("unknown event: " + event);
        }

        return eventableMap.get(event);
    }

    public void actuallySendToPerson(String event, Entity thing) {
        final Eventable eventable = eventableMap.get(event);

        // Try sending a push
        // If that doesn't work, try sending email

        try {
            eventable.makePush(thing);
        } catch (Exception e) {
            eventable.makeEmail(thing);
        }
    }
}
