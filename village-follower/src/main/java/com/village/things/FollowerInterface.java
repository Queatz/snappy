package com.village.things;

import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.view.EarthViewer;

/**
 * Created by jacob on 5/9/16.
 */
public class FollowerInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                EarthThing follow = as.s(EarthStore.class).get(as.getRoute().get(0));

                return as.s(EarthViewer.class).getViewForEntityOrThrow(follow).toJson();
            default:
                throw new NothingLogicResponse("follower - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }
}
