package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 10/7/17.
 */

public class ModeEditor extends EarthControl {
    private final EarthStore earthStore;

    public ModeEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newMode(@NotNull String name, @NotNull String about, @NotNull EarthThing creator) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.MODE_KIND))
                .set(EarthField.PHOTO, false)
                .set(EarthField.NAME, name)
                .set(EarthField.SOURCE, creator.key())
                .set(EarthField.ABOUT, about));
    }
}
