package com.queatz.snappy.logic.editors;

import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.api.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 6/4/17.
 */

public class FormEditor extends EarthControl {
    public FormEditor(@NotNull EarthAs as) {
        super(as);
    }

    public EarthThing newForm(@NotNull EarthThing creator, @NotNull String name) {
        EarthStore earthStore = use(EarthStore.class);

        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.FORM_KIND))
                        .set(EarthField.NAME, name)
                        .set(EarthField.SOURCE, creator.key().name()));
    }

    public EarthThing edit(EarthThing resource, String name, String about, String data) {
        EarthStore earthStore = use(EarthStore.class);

        if (name == null && about == null) {
            throw new NothingLogicResponse("form - nothing to do");
        }

        EarthThing.Builder edit = earthStore.edit(resource);

        if (name != null) {
            edit.set(EarthField.NAME, name);
        }

        if (about != null) {
            edit.set(EarthField.ABOUT, about);
        }

        if (data != null) {
            edit.set(EarthField.DATA, data);
        }

        return earthStore.save(edit);
    }
}
