package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by jacob on 8/20/17.
 */

public class ClubEditor extends EarthControl {
    private final EarthStore earthStore;

    public ClubEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newClub(@NotNull String name, @NotNull EarthThing primaryOwner) {
        EarthThing club = earthStore.save(earthStore.edit(earthStore.create(EarthKind.CLUB_KIND))
                .set(EarthField.NAME, name)
                .set(EarthField.ABOUT, "")
                .set(EarthField.SOURCE, primaryOwner.key()));


        // A club has to be part of itself
        earthStore.addToClub(club, club);

        return club;
    }

    public EarthThing edit(@NotNull EarthThing club,
                           @Nullable String name,
                           @Nullable String about) {
        EarthThing.Builder edit = earthStore.edit(club);

        if (name != null) {
            edit.set(EarthField.NAME, name);
        }

        if (about != null) {
            edit.set(EarthField.ABOUT, about);
        }

        return earthStore.save(edit);
    }
}
