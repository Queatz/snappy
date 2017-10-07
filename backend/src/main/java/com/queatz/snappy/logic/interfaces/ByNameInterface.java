package com.queatz.snappy.logic.interfaces;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.view.EarthViewer;
import com.village.things.PersonMine;

/**
 * Created by jacob on 5/14/16.
 */
public class ByNameInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                return getPersonByName(as, as.getRoute().get(1));
        }

        throw new NothingLogicResponse("by-name - bad path");
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }

    private String getPersonByName(EarthAs as, String personName) {
        EarthThing person = as.s(PersonMine.class).byGoogleUrl(personName.toLowerCase());

        if (person == null) {
            throw new NothingLogicResponse("by name - nobody");
        }

        return as.s(EarthViewer.class).getViewForEntityOrThrow(person).toJson();
    }
}
