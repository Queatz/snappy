package com.queatz.snappy.team.actions;

import android.content.Intent;

import com.queatz.snappy.shared.Config;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class ChangeLocationPhotoAction extends ActivityAction {

    private final DynamicRealmObject location;

    public ChangeLocationPhotoAction(DynamicRealmObject location) {
        this.location = location;
    }

    @Override
    public void execute() {
        getTeam().action.nPendingLocationPhotoChange = location;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        me().getActivity().startActivityForResult(intent, Config.REQUEST_CODE_CHOOSER);
    }
}
