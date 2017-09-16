package com.queatz.snappy.team.actions;

import android.os.Bundle;

import com.queatz.snappy.activity.PersonList;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class ShowBackersAction extends ActivityAction {

    private DynamicRealmObject thing;

    public ShowBackersAction(DynamicRealmObject thing) {
        this.thing = thing;
    }

    @Override
    protected void execute() {
        Bundle bundle = new Bundle();
        bundle.putString("person", thing.getString(Thing.ID));
        bundle.putBoolean("showFollowing", false);
        getTeam().view.show(me().getActivity(), PersonList.class, bundle);
    }
}
