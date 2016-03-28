package com.queatz.snappy.earth.view;

import com.queatz.snappy.earth.access.NothingEarthException;
import com.queatz.snappy.earth.concept.KindView;
import com.queatz.snappy.earth.thing.ClubThing;
import com.queatz.snappy.earth.thing.UpdateRelation;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 3/27/16.
 */

@KindView(ClubThing.class)
public class ClubThingView extends ThingView {

    public ClubThingView(@Nonnull ClubThing club) {
        super(club);
    }

    @KindViewGetter("updates")
    public List<UpdateRelation> getUpdates() {
        return Collections.emptyList();
    }

    @KindViewGetter("messages")
    public List<UpdateRelation> getMessages() {
        throw new NothingEarthException();
    }
}
