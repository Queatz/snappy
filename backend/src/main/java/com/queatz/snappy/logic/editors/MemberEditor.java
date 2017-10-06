package com.queatz.snappy.logic.editors;

import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.api.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 4/9/17.
 */

public class MemberEditor extends EarthControl {
    public MemberEditor(@NotNull EarthAs as) {
        super(as);
    }

    public EarthThing create(EarthThing source, EarthThing target, String status) {
        return create(source, target, status, null);
    }
    public EarthThing create(EarthThing source, EarthThing target, String status, String role) {
        EarthStore earthStore = use(EarthStore.class);

        EarthThing member = earthStore.save(earthStore.edit(earthStore.create(EarthKind.MEMBER_KIND))
                .set(EarthField.STATUS, status)
                .set(EarthField.ROLE, role)
                .set(EarthField.SOURCE, source.key())
                .set(EarthField.TARGET, target.key()));

        if (EarthKind.CLUB_KIND.equals(target.getString(EarthField.KIND))) {
            // Add people to clubs
            if (EarthKind.PERSON_KIND.equals(source.getString(EarthField.KIND))) {
                earthStore.addToClub(source, target);
            }
        } else {
            // Configured on frontend
            // Make thing visible to clubs of parent, i.e. update -> project
            // use(ClubMine.class)
            //         .clubsOf(target)
            //         .forEach(club -> earthStore.addToClub(source, club));
        }

        return member;
    }

    public EarthThing editRole(EarthThing member, String role) {
        EarthStore earthStore = use(EarthStore.class);

        return earthStore.save(earthStore.edit(member)
                .set(EarthField.ROLE, role));
    }
}
