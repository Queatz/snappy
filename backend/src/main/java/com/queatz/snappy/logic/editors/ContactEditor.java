package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 5/8/16.
 */
public class ContactEditor extends EarthControl {
    private final EarthStore earthStore;

    public ContactEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public Entity newContact(@Nonnull Entity thing, @Nonnull Entity person) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.CONTACT_KIND))
                .set(EarthField.SOURCE, thing.key())
                .set(EarthField.TARGET, person.key()));
    }

    public Entity newContact(@Nonnull Entity thing, @Nonnull Entity person, @Nonnull String role) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.CONTACT_KIND))
                .set(EarthField.SOURCE, thing.key())
                .set(EarthField.TARGET, person.key())
                .set(EarthField.ROLE, role));
    }
}
