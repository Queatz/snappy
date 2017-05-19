package com.queatz.snappy.adapter;

import android.os.Bundle;

import com.queatz.snappy.activity.PersonList;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.ActivityAction;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class ShowLikersAction extends ActivityAction {
    private final DynamicRealmObject update;

    public ShowLikersAction(DynamicRealmObject update) {
        this.update = update;
    }

    @Override
    public void execute() {
        Bundle bundle = new Bundle();
        bundle.putString("update", update.getString(Thing.ID));
        bundle.putBoolean("showLikers", true);

        getTeam().view.show(me().getActivity(), PersonList.class, bundle);
    }
}
