package com.queatz.snappy.team.actions;

import com.queatz.branch.Branch;
import com.queatz.snappy.team.contexts.PersonContext;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 4/2/17.
 */

public class PersonAction extends Branch<DynamicRealmObject> implements PersonContext {
    public PersonAction(DynamicRealmObject person) {
        with(person);
    }

    @Override
    public DynamicRealmObject getPerson() {
        return me();
    }
}
