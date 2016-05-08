package com.queatz.snappy.logic.view;

import com.google.appengine.api.datastore.GeoPt;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

/**
 * Created by jacob on 4/2/16.
 */
public class HubView extends ThingView {

    // TODO
    final String contactId;
    final int followers;
    final int members;
    final String address;
    final GeoPt geo;

    public HubView(Entity hub) {
        super(hub);

        geo = new GeoPt(
                (float) hub.getLatLng(EarthField.GEO).latitude(),
                (float) hub.getLatLng(EarthField.GEO).longitude()
        );

        // Validate that we are actually processing a hub!
        if (!EarthKind.HUB_KIND.equals(kind)) {
            throw new NothingLogicResponse("hub - not a hub");
        }

        address = hub.getString(EarthField.ADDRESS);

        // TODO make this a PersonView
        contactId = "<todo>";

        // TODO
        followers = 2;

        // TODO
        members = 1;
    }
}
