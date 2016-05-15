package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/8/16.
 */
public class UpdateEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newUpdate(Entity person) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.UPDATE_KIND))
                .set(EarthField.SOURCE, person.key()));
    }

    public Entity setMessage(Entity update, String message) {
        return earthStore.save(earthStore.edit(update).set(EarthField.MESSAGE, message));
    }

    public Entity newUpdate(Entity person, String action, Entity target) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.UPDATE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.ACTION, action)
                .set(EarthField.TARGET, target.key()));
    }
}
