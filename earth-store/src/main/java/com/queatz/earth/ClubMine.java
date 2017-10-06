package com.queatz.earth;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.queatz.earth.EarthStore.CLUB_GRAPH;

/**
 * Created by jacob on 8/20/17.
 */

public class ClubMine extends EarthControl {
    public ClubMine(@NotNull EarthAs as) {
        super(as);
    }

    public List<EarthThing> clubsOf(EarthThing thing) {
        return use(EarthStore.class).queryRaw(
                new EarthQuery(as)
                        .in("outbound @id graph @graph")
                        .filter(EarthField.KIND, "@club_kind")
                        .distinct(true)
                        .sort("x." + EarthField.CREATED_ON + " desc")
                        .aql(),
                ImmutableMap.of(
                    "id", thing.id(),
                    "graph", CLUB_GRAPH,
                    "club_kind", EarthKind.CLUB_KIND
                ));
    }
}
