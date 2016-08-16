package com.queatz.snappy.team;

import android.support.annotation.NonNull;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 3/19/15.
 */
public class Local {
    public Team team;

    public Local(Team t) {
        team = t;
    }

    public void updateRecentsForMessage(@NonNull DynamicRealmObject message) {
        RealmResults<DynamicRealmObject> recents = team.realm.where("Thing")
                .beginGroup()
                    .equalTo("from.id", message.getObject(Thing.FROM).getString(Thing.ID))
                    .equalTo("to.id", message.getObject(Thing.TO).getString(Thing.ID))
                .endGroup()
                .or()
                .beginGroup()
                    .equalTo("to.id", message.getObject(Thing.FROM).getString(Thing.ID))
                    .equalTo("from.id", message.getObject(Thing.TO).getString(Thing.ID))
                .endGroup()
                .findAll();

        team.realm.beginTransaction();

        for(int i = 0; i < recents.size(); i++) {
            DynamicRealmObject recent = recents.get(i);
            recent.setObject(Thing.LATEST, message);

            if(!team.auth.getUser().equals(recent.getObject(Thing.SOURCE).getString(Thing.ID))) {
                recent.setBoolean(Thing.SEEN, false);
            }
        }

        team.realm.commitTransaction();
    }
}
