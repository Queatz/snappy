package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 5/14/16.
 */
public class JoinEditor extends EarthControl {
    private final EarthStore earthStore;
    private final JoinMine joinMine;

    public JoinEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
        joinMine = use(JoinMine.class);
    }

    public EarthThing newJoin(EarthThing person, EarthThing party) {
        // Joins are unique
        EarthThing join = joinMine.byPersonAndParty(person, party);

        if (join == null) {
            join = earthStore.create(EarthKind.JOIN_KIND);
        }

        return earthStore.save(earthStore.edit(join)
                .set(EarthField.STATUS, Config.JOIN_STATUS_REQUESTED)
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.TARGET, party.key()));
    }

    public void hide(EarthThing join) {
        earthStore.save(earthStore.edit(join).set(EarthField.STATUS, Config.JOIN_STATUS_OUT));
    }

    public void accept(EarthThing join) {
        earthStore.save(earthStore.edit(join).set(EarthField.STATUS, Config.JOIN_STATUS_IN));
    }

    public EarthThing setStatus(EarthThing join, String status) {
        return earthStore.save(earthStore.edit(join).set(EarthField.STATUS, status));
    }
}
