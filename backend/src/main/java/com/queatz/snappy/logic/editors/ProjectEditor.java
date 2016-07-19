package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 5/23/16.
 */
public class ProjectEditor extends EarthControl {
    private final EarthStore earthStore;

    public ProjectEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public Entity newProject(@Nonnull String name, Entity primaryOwner) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.PROJECT_KIND))
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
