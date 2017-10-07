package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.exceptions.NothingLogicResponse;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 5/23/16.
 */
public class ProjectEditor extends EarthControl {
    private final EarthStore earthStore;

    public ProjectEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newProject(@NotNull String name, EarthThing primaryOwner) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.PROJECT_KIND))
                .set(EarthField.PHOTO, false)
                .set(EarthField.NAME, name)
                .set(EarthField.SOURCE, primaryOwner.key())
                .set(EarthField.ABOUT, ""));
    }

    public EarthThing edit(EarthThing resource, String name, String about) {
        if (name == null && about == null) {
            throw new NothingLogicResponse("project - nothing to do");
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
