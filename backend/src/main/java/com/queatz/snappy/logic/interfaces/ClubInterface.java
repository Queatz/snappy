package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.editors.ClubEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

/**
 * Created by jacob on 8/20/17.
 */

public class ClubInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
        String[] name = as.getParameters().get(EarthField.NAME);

        if (name == null || name.length != 1) {
            throw new NothingLogicResponse("club - name wass expected");
        }

        return new ClubEditor(as).newClub(name[0], as.getUser());
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing club) {
        String[] name = as.getParameters().get(EarthField.NAME);
        String[] about = as.getParameters().get(EarthField.ABOUT);

        return new ClubEditor(as).edit(club, extract(name), extract(about));
    }
}
