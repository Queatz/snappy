package com.queatz.snappy.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.queatz.snappy.activity.Person;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.ActivityAction;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class OpenMessagesAction extends ActivityAction {

    private final DynamicRealmObject person;
    private final String message;

    public OpenMessagesAction(@NonNull DynamicRealmObject person) {
        this(person, null);
    }

    public OpenMessagesAction(@NonNull DynamicRealmObject person, @Nullable String message) {
        this.person = person;
        this.message = message;
    }

    @Override
    protected void execute() {
        if (me().getActivity() instanceof Person) {
            DynamicRealmObject activityPerson = ((Person) me().getActivity()).getPerson();

            if (activityPerson != null && activityPerson.getString(Thing.ID).equals(person.getString(Thing.ID))) {
                ((Person) me().getActivity()).getSlideScreen().setSlide(Person.SLIDE_MESSAGES);
            }

            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("person", person.getString(Thing.ID));
        bundle.putString("show", "messages");

        if (message != null) {
            bundle.putString("message", message);
        }

        getTeam().view.show(me().getActivity(), Person.class, bundle);
    }
}
