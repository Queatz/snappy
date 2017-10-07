package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.view.EarthView;

/**
 * Created by jacob on 4/2/16.
 */
public class HubView extends CommonThingView {

    final String address;
    final EarthGeo geo;

    public HubView(EarthAs as, EarthThing hub) {
        this(as, hub, EarthView.DEEP);
    }

    public HubView(EarthAs as, EarthThing hub, EarthView view) {
        super(as, hub, view);

        geo = hub.getGeo(EarthField.GEO);
        address = hub.getString(EarthField.ADDRESS);
    }
}
