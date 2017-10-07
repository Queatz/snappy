package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.plugins.ContactEditorPlugin;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 5/8/16.
 */
public class ContactEditor extends EarthControl implements ContactEditorPlugin {
    private final EarthStore earthStore;

    public ContactEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    @Override
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
