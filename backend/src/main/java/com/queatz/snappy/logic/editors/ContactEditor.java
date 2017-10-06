package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 5/8/16.
 */
public class ContactEditor extends EarthControl {
    private final EarthStore earthStore;

    public ContactEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newContact(@NotNull EarthThing thing, @NotNull EarthThing person) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.CONTACT_KIND))
                .set(EarthField.SOURCE, thing.key())
                .set(EarthField.TARGET, person.key()));
    }

    public EarthThing newContact(@NotNull EarthThing thing, @NotNull EarthThing person, @NotNull String role) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.CONTACT_KIND))
                .set(EarthField.SOURCE, thing.key())
                .set(EarthField.TARGET, person.key())
                .set(EarthField.ROLE, role));
    }
}
