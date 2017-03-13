package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.PersonMine;

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
        EarthThing person = new PersonMine(as).byGoogleUrl(personName.toLowerCase());

        if (person == null) {
            throw new NothingLogicResponse("by name - nobody");
        }

        return new EarthViewer(as).getViewForEntityOrThrow(person).toJson();
    }
}
