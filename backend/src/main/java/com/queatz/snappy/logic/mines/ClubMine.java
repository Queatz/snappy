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
                "x." + EarthField.KIND + " == @kind and " +
                "x." + EarthField.SOURCE + " == @source and " +
                "(for y in " + EarthStore.DEFAULT_COLLECTION + " filter y._key == x." + EarthField.TARGET + " return y." + EarthField.KIND + ")[0] == @club_kind",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MEMBER_KIND,
                        "club_kind", EarthKind.CLUB_KIND,
                        "source", thing.key().name()
                ));
    }
}
