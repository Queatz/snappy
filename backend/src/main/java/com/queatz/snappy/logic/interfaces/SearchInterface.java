package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.view.EarthView;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.village.things.RecentMine;
import com.village.things.EntityListView;
import com.queatz.snappy.shared.Config;

import java.util.List;

/**
 * Created by jacob on 7/9/16.
 */
public class SearchInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        if (!as.getParameters().containsKey(Config.PARAM_LATITUDE)
                || !as.getParameters().containsKey(Config.PARAM_LONGITUDE)) {
            throw new NothingLogicResponse("search - no latitude and longitude");
        }

        String latitudeParam = as.getParameters().get(Config.PARAM_LATITUDE)[0];
        String longitudeParam = as.getParameters().get(Config.PARAM_LONGITUDE)[0];

        String qParam;

        if (as.getParameters().containsKey(Config.PARAM_Q)) {
            qParam = as.getParameters().get(Config.PARAM_Q)[0];
        } else {
            qParam = null;
        }

        float latitude = Float.valueOf(latitudeParam);
        float longitude = Float.valueOf(longitudeParam);

        String kindFilter = null;

        if (as.getRoute().size() > 1) {
            kindFilter = as.getRoute().get(1);
        }

        final EarthGeo latLng = EarthGeo.of(latitude, longitude);

        List<EarthThing> results = as.s(EarthStore.class).getNearby(latLng, kindFilter, qParam);

        // Hack to include recents for now...need to find a better way to "pick people" that are not nearby
        if (as.hasUser() && kindFilter != null && kindFilter.contains(EarthKind.PERSON_KIND)) {
            List<EarthThing> recents = as.s(RecentMine.class).forPerson(as.getUser());

            if (qParam == null) {
                results.addAll(recents);
            } else {
                EarthStore earthStore = as.s(EarthStore.class);

                for (EarthThing recent : recents) {
                    EarthThing with = earthStore.get(recent.getKey(EarthField.TARGET));
                    if (with != null && (
                            with.getString(EarthField.FIRST_NAME).toLowerCase().startsWith(qParam.toLowerCase()) ||
                                    with.getString(EarthField.LAST_NAME).toLowerCase().startsWith(qParam.toLowerCase())
                    )) {
                        boolean duplicate = false;
                        for (EarthThing result : results) {
                            if (result.key().equals(with.key())) {
                                duplicate = true;
                                break;
                            }
                        }

                        if (duplicate) {
                            continue;
                        }

                        results.add(with);
                    }
                }
            }
        }

        return new EntityListView(as, results, EarthView.SHALLOW).toJson();
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }
}
