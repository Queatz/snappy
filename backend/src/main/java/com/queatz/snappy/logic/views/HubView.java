package com.queatz.snappy.logic.views;

import com.google.appengine.api.datastore.GeoPt;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.ContactMine;
import com.queatz.snappy.logic.mines.FollowerMine;
import com.queatz.snappy.logic.mines.UpdateMine;

import java.util.List;

/**
 * Created by jacob on 4/2/16.
 */
public class HubView extends CommonThingView {

    final String address;
    final GeoPt geo;

    public HubView(Entity hub) {
        this(hub, EarthView.DEEP);
    }

    public HubView(Entity hub, EarthView view) {
        super(hub, view);

        geo = new GeoPt(
            (float) hub.getLatLng(EarthField.GEO).latitude(),
            (float) hub.getLatLng(EarthField.GEO).longitude()
        );

        address = hub.getString(EarthField.ADDRESS);
    }
}
