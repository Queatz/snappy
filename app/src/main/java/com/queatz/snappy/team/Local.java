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
        updateRecentsForMessage(message, false);
    }

    public void updateRecentsForMessage(@NonNull DynamicRealmObject message, boolean inTransaction) {
        RealmResults<DynamicRealmObject> recents = team.realm.where("Thing")
                .equalTo(Thing.KIND, "recent")
                .beginGroup()
                    .beginGroup()
                        .equalTo("latest.from.id", message.getObject(Thing.FROM).getString(Thing.ID))
                        .equalTo("latest.to.id", message.getObject(Thing.TO).getString(Thing.ID))
                    .endGroup()
                    .or()
                    .beginGroup()
                        .equalTo("latest.to.id", message.getObject(Thing.FROM).getString(Thing.ID))
                        .equalTo("latest.from.id", message.getObject(Thing.TO).getString(Thing.ID))
                    .endGroup()
                .endGroup()
                .findAll();

        if (!inTransaction) {
            team.realm.beginTransaction();
        }

        for(int i = 0; i < recents.size(); i++) {
            DynamicRealmObject recent = recents.get(i);
            recent.setObject(Thing.LATEST, message);

            if(!team.auth.getUser().equals(message.getObject(Thing.FROM).getString(Thing.ID))) {
                recent.setBoolean(Thing.SEEN, false);
            }
        }

        if (!inTransaction) {
            team.realm.commitTransaction();
        }
    }
}
