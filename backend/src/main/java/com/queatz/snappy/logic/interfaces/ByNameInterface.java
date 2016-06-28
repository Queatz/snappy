package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.PersonMine;
import com.queatz.snappy.logic.views.PersonView;

/**
 * Created by jacob on 5/14/16.
 */
public class ByNameInterface implements Interfaceable {
    PersonMine personMine = EarthSingleton.of(PersonMine.class);

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
        Entity person = personMine.byGoogleUrl(personName.toLowerCase());

        if (person == null) {
            throw new NothingLogicResponse("by name - nobody");
        }

        return new PersonView(person).toJson();
    }
}
