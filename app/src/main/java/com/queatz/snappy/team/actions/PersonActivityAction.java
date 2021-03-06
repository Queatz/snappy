package com.queatz.snappy.team.actions;

import com.queatz.snappy.team.contexts.PersonContext;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 4/2/17.
 */

class PersonActivityAction extends ActivityAction implements PersonContext {

    private DynamicRealmObject person;

    public PersonActivityAction(DynamicRealmObject person) {
        this.person = person;
    }

    @Override
    public DynamicRealmObject getPerson() {
        return person;
    }
}
