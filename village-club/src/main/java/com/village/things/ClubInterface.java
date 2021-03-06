package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.plugins.MemberEditorPlugin;
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

        EarthThing club = as.s(ClubEditor.class).newClub(name[0], as.getUser());

        // Add yourself as the first club member
        as.s(MemberEditorPlugin.class).create(as.getUser(), club, Config.MEMBER_STATUS_ACTIVE);

        return club;
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing club) {
        String[] name = as.getParameters().get(EarthField.NAME);
        String[] about = as.getParameters().get(EarthField.ABOUT);

        return as.s(ClubEditor.class).edit(club, extract(name), extract(about));
    }
}
