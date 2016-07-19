package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.mines.JoinMine;
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

    public Entity newJoin(Entity person, Entity party) {
        // Joins are unique
        Entity join = joinMine.byPersonAndParty(person, party);

        if (join == null) {
            join = earthStore.create(EarthKind.JOIN_KIND);
        }

        return earthStore.save(earthStore.edit(join)
                .set(EarthField.STATUS, Config.JOIN_STATUS_REQUESTED)
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.TARGET, party.key()));
    }

    public void hide(Entity join) {
        earthStore.save(earthStore.edit(join).set(EarthField.STATUS, Config.JOIN_STATUS_OUT));
    }

    public void accept(Entity join) {
        earthStore.save(earthStore.edit(join).set(EarthField.STATUS, Config.JOIN_STATUS_IN));
    }

    public Entity setStatus(Entity join, String status) {
        return earthStore.save(earthStore.edit(join).set(EarthField.STATUS, status));
    }
}
