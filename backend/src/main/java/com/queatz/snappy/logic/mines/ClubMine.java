package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by jacob on 8/20/17.
 */

public class ClubMine extends EarthControl {
    public ClubMine(@NotNull EarthAs as) {
        super(as);
    }

    public List<EarthThing> clubsOf(EarthThing thing) {
        return use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @club_kind and " +
                "(for y in " + EarthStore.DEFAULT_COLLECTION +
                " filter y." + EarthField.KIND + " == @member_kind and y." + EarthField.SOURCE +
                " == @source and y." + EarthField.TARGET + " == x._key return y)[0] != null",
                ImmutableMap.of(
                        "member_kind", EarthKind.MEMBER_KIND,
                        "club_kind", EarthKind.CLUB_KIND,
                        "source", thing.key().name()
                ));
    }
}
