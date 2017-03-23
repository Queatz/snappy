package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceEditor extends EarthControl {
    private final EarthStore earthStore;

    public ResourceEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newResource(@NotNull String name, EarthThing primaryOwner) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.RESOURCE_KIND))
                .set(EarthField.PHOTO, false)
                .set(EarthField.NAME, name)
                .set(EarthField.SOURCE, primaryOwner.key())
                .set(EarthField.ABOUT, ""));
    }

    public EarthThing edit(EarthThing resource, String name, String about) {
        if (name == null && about == null) {
            throw new NothingLogicResponse("resource - nothing to do");
        }

        EarthThing.Builder edit = earthStore.edit(resource);

        if (name != null) {
            edit.set(EarthField.NAME, name);
        }

        if (about != null) {
            edit.set(EarthField.ABOUT, about);
        }

        return earthStore.save(edit);
    }
}
