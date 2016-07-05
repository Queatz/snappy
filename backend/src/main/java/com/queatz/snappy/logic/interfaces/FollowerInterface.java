package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.FollowerView;

/**
 * Created by jacob on 5/9/16.
 */
public class FollowerInterface implements Interfaceable {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                Entity follow = earthStore.get(as.getRoute().get(0));

                return new FollowerView(follow).toJson();
            default:
                throw new NothingLogicResponse("follower - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }
}
