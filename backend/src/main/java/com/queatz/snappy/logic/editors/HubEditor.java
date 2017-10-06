package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by jacob on 4/4/16.
 */
public class HubEditor extends EarthControl {
    private final EarthStore earthStore;

    public HubEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newHub(@NotNull String name,
                             @NotNull String address,
                             @NotNull EarthGeo latLng,
                             @NotNull EarthThing primaryOwner) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.HUB_KIND))
                .set(EarthField.PHOTO, false)
                .set(EarthField.NAME, name)
                .set(EarthField.ABOUT, "")
                .set(EarthField.GEO, latLng)
                .set(EarthField.SOURCE, primaryOwner.key())
                .set(EarthField.ADDRESS, address));
    }

    public EarthThing edit(@NotNull EarthThing hub,
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
