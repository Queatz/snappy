package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by jacob on 4/4/16.
 */
public class HubEditor extends EarthControl {
    private final EarthStore earthStore;

    public HubEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newHub(@Nonnull String name, @Nonnull String address, @Nonnull EarthGeo latLng) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.HUB_KIND))
                .set(EarthField.PHOTO, false)
                .set(EarthField.NAME, name)
                .set(EarthField.ABOUT, "")
                .set(EarthField.GEO, latLng)
                .set(EarthField.ADDRESS, address));
    }

    public EarthThing edit(@Nonnull EarthThing hub,
                       @Nullable String name,
                       @Nullable String address,
                       @Nullable EarthGeo latLng,
                       @Nullable String about) {
        EarthThing.Builder edit = earthStore.edit(hub);

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
