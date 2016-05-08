package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 5/8/16.
 */
public class ContactEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newContact(@Nonnull Entity thing, @Nonnull Entity person) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.CONTACT_KIND))
                .set(EarthField.SOURCE, thing)
                .set(EarthField.TARGET, person));
    }
}
