package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.mines.ContactMine;
import com.queatz.snappy.logic.mines.UpdateMine;

import java.util.List;

/**
 * Created by jacob on 5/22/16.
 */
public class CommonThingView extends ThingView {
    final List<Viewable> contacts;
    final List<Viewable> updates;
    final int followers;

    public CommonThingView(Entity thing) {
        this(thing, EarthView.DEEP);
    }

    public CommonThingView(Entity thing, EarthView view) {
        super(thing, view);

        // TODO
        followers = -0;//EarthSingleton.of(FollowerMine.class).countFollowers(hub);

        switch (view) {
            case DEEP:
                final ContactMine contactMine = EarthSingleton.of(ContactMine.class);
                final UpdateMine updateMine = EarthSingleton.of(UpdateMine.class);
                final List<Entity> contacts = contactMine.getContacts(thing);
                final List<Entity> updates = updateMine.updatesOf(thing);

                if (contacts != null) {
                    this.contacts = new EntityListView(contacts, EarthView.SHALLOW).asList();
                } else {
                    this.contacts = null;
                }


                if (updates != null) {
                    this.updates = new EntityListView(updates, EarthView.SHALLOW).asList();
                } else {
                    this.updates = null;
                }

                break;
            default:
                this.contacts = null;
                this.updates = null;
        }
    }
}
