package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/14/16.
 */
public class PartyView extends ThingView {

    public PartyView(Entity party) {
        this(party, EarthView.DEEP);
    }

    public PartyView(Entity party, EarthView view) {
        super(party, view);
    }
}
