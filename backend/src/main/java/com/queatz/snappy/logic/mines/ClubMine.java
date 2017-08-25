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

import static com.queatz.snappy.logic.EarthStore.CLUB_GRAPH;
import static com.queatz.snappy.logic.EarthStore.DEFAULT_FIELD_CONCLUDED;

/**
 * Created by jacob on 8/20/17.
 */

public class ClubMine extends EarthControl {
    public ClubMine(@NotNull EarthAs as) {
        super(as);
    }

    public List<EarthThing> clubsOf(EarthThing thing) {
        return use(EarthStore.class).queryRaw(
                "for club in inbound @id graph @graph filter" +
                " club.@kind == @club_kind and club.@concluded_on == null return club",
                ImmutableMap.of(
                    "id", thing.id(),
                    "graph", CLUB_GRAPH,
                    "kind", EarthField.KIND,
                    "club_kind", EarthKind.CLUB_KIND,
                    "concluded_on", DEFAULT_FIELD_CONCLUDED
                ));
    }
}
