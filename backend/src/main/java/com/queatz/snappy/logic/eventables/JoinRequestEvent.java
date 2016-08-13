package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class JoinRequestEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    Entity join;

    // Serialization

    public JoinRequestEvent() {}

    public JoinRequestEvent fromData(String data) {
        join = earthStore.get(data);
        return this;
    }

    public String toData() {
        return join.key().name();
    }

    // End Serialization

    public JoinRequestEvent(Entity join) {
        this.join = join;
    }

    @Override
    public Object makePush() {
        return new PushSpec<>(
                Config.PUSH_ACTION_JOIN_REQUEST,
                ImmutableMap.of(
                        "id", join.key().name(),
                        "person", join.getKey(EarthField.SOURCE).name(), // go deeper {name: ...}
                        "party", join.getKey(EarthField.TARGET).name() // go deeper {name: ...}
                )
        );
    }

    @Override
    public String makeSubject() {
        return null;
    }

    @Override
    public String makeEmail() {
        return null;
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}
