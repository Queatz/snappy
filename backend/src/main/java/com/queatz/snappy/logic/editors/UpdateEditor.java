package com.queatz.snappy.logic.editors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.api.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 5/8/16.
 */
public class UpdateEditor extends EarthControl {
    private final EarthStore earthStore;
    private final JoinEditor joinEditor;

    public UpdateEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
        joinEditor = use(JoinEditor.class);
    }

    public EarthThing newUpdate(EarthThing person) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.UPDATE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.NAME)
                .set(EarthField.ABOUT)
                .set(EarthField.PHOTO, false)
                .set(EarthField.TARGET, person.key()));
    }

    public EarthThing stageUpdate(EarthThing person) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.UPDATE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.NAME));
    }

    public EarthThing setMessage(EarthThing update, String message) {
        return earthStore.save(earthStore.edit(update).set(EarthField.ABOUT, message));
    }

    public EarthThing newUpdate(EarthThing person, String action, EarthThing target) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.UPDATE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.ACTION, action)
                .set(EarthField.TARGET, target.key()));
    }

    public EarthThing updateWith(EarthThing update, EarthThing thing, String message, boolean photo, EarthGeo geo, JsonArray with, boolean going) {
        EarthThing.Builder edit = earthStore.edit(update)
                .set(EarthField.TARGET, thing.key())
                .set(EarthField.ACTION, Config.UPDATE_ACTION_UPTO)
                .set(EarthField.GOING, going)
                .set(EarthField.PHOTO, photo);

        if (message == null) {
            edit.set(EarthField.ABOUT);
        } else {
            edit.set(EarthField.ABOUT, message);
        }

        if (geo != null) {
            edit.set(EarthField.GEO, geo);
        }

        EarthThing saved = earthStore.save(edit);

        if (with != null && with.size() > 0) {
            for (JsonElement peep : with) {
                EarthThing person = earthStore.get(peep.getAsString());
                joinEditor.newJoin(person, saved);
            }
        }

        return saved;
    }

    public EarthThing updateWith(EarthThing update, String message, boolean photo) {
        EarthThing.Builder edit = earthStore.edit(update)
                .set(EarthField.PHOTO, photo);

        if (message == null) {
            edit.set(EarthField.ABOUT);
        } else {
            edit.set(EarthField.ABOUT, message);
        }

        return earthStore.save(edit);
    }
}
