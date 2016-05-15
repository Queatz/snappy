package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/8/16.
 */
public class MessageEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newMessage(Entity source, Entity target, String message) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.MESSAGE_KIND))
                .set(EarthField.SOURCE, source.key())
                .set(EarthField.TARGET, target.key())
                .set(EarthField.MESSAGE, message));
    }
}
