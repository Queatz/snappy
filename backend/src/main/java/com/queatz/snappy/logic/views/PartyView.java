package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/14/16.
 */
public class PartyView extends ThingView {

    public PartyView(EarthAs as, Entity party) {
        this(as, party, EarthView.DEEP);
    }

    public PartyView(EarthAs as, Entity party, EarthView view) {
        super(as, party, view);
    }
}
