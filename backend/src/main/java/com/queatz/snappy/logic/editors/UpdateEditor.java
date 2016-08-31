package com.queatz.snappy.logic.editors;

import com.google.appengine.api.datastore.GeoPt;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.google.cloud.datastore.NullValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
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

    public Entity newUpdate(Entity person) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.UPDATE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.NAME, NullValue.of())
                .set(EarthField.ABOUT, NullValue.of())
                .set(EarthField.PHOTO, false)
                .set(EarthField.TARGET, person.key()));
    }

    public Entity stageUpdate(Entity person) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.UPDATE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.NAME, NullValue.of()));
    }

    public Entity setMessage(Entity update, String message) {
        return earthStore.save(earthStore.edit(update).set(EarthField.ABOUT, message));
    }

    public Entity newUpdate(Entity person, String action, Entity target) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.UPDATE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.ACTION, action)
                .set(EarthField.TARGET, target.key()));
    }

    public Entity updateWith(Entity update, Entity thing, String message, boolean photo, LatLng geo, JsonArray with) {
        Entity.Builder edit = earthStore.edit(update)
                .set(EarthField.TARGET, thing.key())
                .set(EarthField.ACTION, Config.UPDATE_ACTION_UPTO)
                .set(EarthField.PHOTO, photo);

        if (message == null) {
            edit.set(EarthField.ABOUT, NullValue.of());
        } else {
            edit.set(EarthField.ABOUT, message);
        }

        if (geo != null) {
            edit.set(EarthField.GEO, geo);
        }

        Entity saved = earthStore.save(edit);

        if (with != null && with.size() > 0) {
            for (JsonElement peep : with) {
                Entity person = earthStore.get(peep.getAsString());
                joinEditor.newJoin(person, saved);
            }
        }

        return saved;
    }

    public Entity updateWith(Entity update, String message, boolean photo) {
        Entity.Builder edit = earthStore.edit(update)
                .set(EarthField.PHOTO, photo);

        if (message == null) {
            edit.set(EarthField.ABOUT, NullValue.of());
        } else {
            edit.set(EarthField.ABOUT, message);
        }

        return earthStore.save(edit);
    }
}
