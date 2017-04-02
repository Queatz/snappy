package com.queatz.snappy.team.actions;

import android.os.Bundle;

import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 4/2/17.
 */

public class OpenProfileAction extends PersonActivityAction {

    public OpenProfileAction(DynamicRealmObject person) {
        super(person);
    }

    @Override
    public void execute() {
        if (getPerson() == null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(Config.EXTRA_PERSON_ID, getPerson().getString(Thing.ID));
        getTeam().view.show(me(), Person.class, bundle);
    }
}
