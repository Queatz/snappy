package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by jacob on 4/4/16.
 */
public class HubEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newHub(@Nonnull String name, @Nonnull String address, @Nonnull LatLng latLng) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.HUB_KIND))
                .set(EarthField.PHOTO, false)
                .set(EarthField.NAME, name)
                .set(EarthField.ABOUT, "")
                .set(EarthField.GEO, latLng)
                .set(EarthField.ADDRESS, address));
    }

    public Entity edit(@Nonnull Entity hub,
                       @Nullable String name,
                       @Nullable String address,
                       @Nullable LatLng latLng,
                       @Nullable String about) {
        Entity.Builder edit = earthStore.edit(hub);

        if (name != null) {
            edit.set(EarthField.NAME, name);
        }

        if (about != null) {
            edit.set(EarthField.ABOUT, about);
        }

        if (address != null) {
            edit.set(EarthField.ADDRESS, address);
        }

        if (latLng != null) {
            edit.set(EarthField.GEO, latLng);
        }

        return earthStore.save(edit);
    }
}
