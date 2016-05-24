package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newResource(@Nonnull String name, Entity primaryOwner) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.RESOURCE_KIND))
                .set(EarthField.PHOTO, false)
                .set(EarthField.NAME, name)
                .set(EarthField.SOURCE, primaryOwner.key())
                .set(EarthField.ABOUT, ""));
    }

    public Entity edit(Entity resource, String name, String about) {
        if (name == null && about == null) {
            throw new NothingLogicResponse("resource - nothing to do");
        }

        Entity.Builder edit = earthStore.edit(resource);

        if (name != null) {
            edit.set(EarthField.NAME, name);
        }

        if (about != null) {
            edit.set(EarthField.ABOUT, about);
        }

        return earthStore.save(edit);
    }
}
