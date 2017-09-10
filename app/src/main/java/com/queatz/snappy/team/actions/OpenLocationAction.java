package com.queatz.snappy.team.actions;

import android.content.Intent;
import android.net.Uri;

import com.queatz.snappy.util.Functions;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class OpenLocationAction extends ActivityAction {
    private final DynamicRealmObject location;

    public OpenLocationAction(DynamicRealmObject location) {
        this.location = location;
    }

    @Override
    protected void execute() {
        if(location == null)
            return;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Functions.getLocationText(location)));

        me().getActivity().startActivity(intent);
    }
}
