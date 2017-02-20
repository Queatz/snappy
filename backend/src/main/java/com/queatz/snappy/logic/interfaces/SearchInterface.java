package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.RecentMine;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.shared.Config;

import java.util.ArrayList;
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

        final LatLng latLng = LatLng.of(latitude, longitude);

        List<Entity> results = new EarthStore(as).getNearby(latLng, kindFilter, qParam);

        // Hack to include recents for now...need to find a better way to "pick people" that are not nearby
        if (kindFilter != null && kindFilter.contains(EarthKind.PERSON_KIND)) {
            List<Entity> recents = new RecentMine(as).forPerson(as.getUser());

            if (qParam == null) {
                results.addAll(recents);
            } else {
                EarthStore earthStore = new EarthStore(as);

                for (Entity recent : recents) {
                    Entity with = earthStore.get(recent.getKey(EarthField.TARGET));
                    if (with != null && (
                            with.getString(EarthField.FIRST_NAME).toLowerCase().startsWith(qParam.toLowerCase()) ||
                                    with.getString(EarthField.LAST_NAME).toLowerCase().startsWith(qParam.toLowerCase())
                    )) {
                        boolean duplicate = false;
                        for (Entity result : results) {
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

        return new EntityListView(as,
                results, EarthView.SHALLOW).toJson();
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }
}
