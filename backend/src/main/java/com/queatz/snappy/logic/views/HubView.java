package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;

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

        geo = new EarthGeo(
            (float) hub.getGeo(EarthField.GEO).getLatitude(),
            (float) hub.getGeo(EarthField.GEO).getLongitude()
        );

        address = hub.getString(EarthField.ADDRESS);
    }
}
