package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.editors.ClubEditor;
import com.queatz.snappy.logic.editors.MemberEditor;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 8/20/17.
 */

public class ClubInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
        String[] name = as.getParameters().get(EarthField.NAME);

        if (name == null || name.length != 1) {
            throw new NothingLogicResponse("club - name was expected");
        }

        EarthThing club = new ClubEditor(as).newClub(name[0], as.getUser());

        // Add yourself as the first club member
        new MemberEditor(as).create(as.getUser(), club, Config.MEMBER_STATUS_ACTIVE);

        return club;
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing club) {
        String[] name = as.getParameters().get(EarthField.NAME);
        String[] about = as.getParameters().get(EarthField.ABOUT);

        return new ClubEditor(as).edit(club, extract(name), extract(about));
    }
}
