package com.queatz.snappy.team.actions;

import android.content.Intent;
import android.provider.CalendarContract;

import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class OpenDateAction extends ActivityAction {
    private final DynamicRealmObject party;

    public OpenDateAction(DynamicRealmObject party) {
        this.party = party;
    }

    @Override
    protected void execute() {
        Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
        intent.setType("vnd.android.cursor.dir/event");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, party.getDate(Thing.DATE).getTime());
        intent.putExtra(CalendarContract.Events.TITLE, party.getString(Thing.NAME));
        intent.putExtra(CalendarContract.Events.DESCRIPTION, party.getString(Thing.ABOUT));

        if (!party.isNull(Thing.LOCATION)) {
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, party.getObject(Thing.LOCATION).getString(Thing.NAME));
        }

        me().getActivity().startActivity(intent);
    }
}
