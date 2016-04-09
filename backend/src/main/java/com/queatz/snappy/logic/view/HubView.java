package com.queatz.snappy.logic.view;

import com.google.gcloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

/**
 * Created by jacob on 4/2/16.
 */
public class HubView extends ThingView {

    // TODO
    final String contactId;
    final int followers;
    final int members;

    public HubView(Entity hub) {
        super(hub);

        // Validate that we are actually processing a hub!
        if (!EarthKind.HUB_KIND.equals(kind)) {
            throw new NothingLogicResponse("hub - not a hub");
        }

        // TODO make this a PersonView
        contactId = "<todo>";

        // TODO
        followers = 2;

        // TODO
        members = 1;
    }
}
