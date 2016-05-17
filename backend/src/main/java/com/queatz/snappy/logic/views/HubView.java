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

import java.util.List;

/**
 * Created by jacob on 4/2/16.
 */
public class HubView extends ThingView {

    // TODO
    final List<Viewable> contacts;
    final int followers;
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

        // TODO
        followers = -0;//EarthSingleton.of(FollowerMine.class).countFollowers(hub);

        switch (view) {
            case DEEP:
                ContactMine contactMine = EarthSingleton.of(ContactMine.class);
                List<Entity> contacts = contactMine.getContacts(hub);

                if (contacts != null) {
                    this.contacts = new EntityListView(contacts, EarthView.SHALLOW).asList();
                } else {
                    this.contacts = null;
                }

                break;
            default:
                this.contacts = null;
        }
    }
}
