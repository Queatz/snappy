package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthStore;
import com.queatz.snappy.api.Interfaceable;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.view.SuccessView;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 5/9/16.
 */
public class RecentInterface implements Interfaceable {
    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_DELETE:
                        new EarthStore(as).conclude(as.getRoute().get(0));
                        return new SuccessView(true).toJson();
                }

                break;
        }

        throw new NothingLogicResponse("recent - bad path");
    }

    @Override
    public String get(EarthAs as) {
        throw new NothingLogicResponse("recent - bad path");

    }
}
