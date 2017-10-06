package com.queatz.snappy.events;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.exceptions.NothingLogicResponse;
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
    public static final Map<Class<? extends Eventable>, String> actionMap = new HashMap<>();

    public static void register(String action, Class<? extends Eventable> eventable) {
        eventableMap.put(action, eventable);
        actionMap.put(eventable, action);
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
