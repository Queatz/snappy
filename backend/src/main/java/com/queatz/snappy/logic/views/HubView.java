package com.queatz.snappy.logic.views;

import com.google.appengine.api.datastore.GeoPt;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 4/2/16.
 */
public class HubView extends CommonThingView {

    final String address;
    final GeoPt geo;

    public HubView(EarthAs as, Entity hub) {
        this(as, hub, EarthView.DEEP);
    }

    public HubView(EarthAs as, Entity hub, EarthView view) {
        super(as, hub, view);

        geo = new GeoPt(
            (float) hub.getLatLng(EarthField.GEO).latitude(),
            (float) hub.getLatLng(EarthField.GEO).longitude()
        );

        address = hub.getString(EarthField.ADDRESS);
    }
}
