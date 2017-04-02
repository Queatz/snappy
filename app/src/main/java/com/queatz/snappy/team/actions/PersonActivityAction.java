package com.queatz.snappy.team.actions;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 4/2/17.
 */

class PersonActivityAction extends ActivityAction {

    private DynamicRealmObject person;

    public PersonActivityAction(DynamicRealmObject person) {
        this.person = person;
    }

    public DynamicRealmObject getPerson() {
        return person;
    }
}
