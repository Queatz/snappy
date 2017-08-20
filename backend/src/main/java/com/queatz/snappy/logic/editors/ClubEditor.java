package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

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
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.CLUB_KIND))
                .set(EarthField.NAME, name)
                .set(EarthField.SOURCE, primaryOwner.key()));
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
