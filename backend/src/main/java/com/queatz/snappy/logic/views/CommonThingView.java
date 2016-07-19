package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.mines.ContactMine;
import com.queatz.snappy.logic.mines.FollowerMine;
import com.queatz.snappy.logic.mines.UpdateMine;

import java.util.List;

/**
 * Created by jacob on 5/22/16.
 */
public class CommonThingView extends ThingView {
    final List<Viewable> contacts;
    final List<Viewable> updates;
    final int followers;

    public CommonThingView(EarthAs as, Entity thing) {
        this(as, thing, EarthView.DEEP);
    }

    public CommonThingView(EarthAs as, Entity thing, EarthView view) {
        super(as, thing, view);

        followers = use(FollowerMine.class).countFollowers(thing);

        switch (view) {
            case DEEP:
                final ContactMine contactMine = use(ContactMine.class);
                final UpdateMine updateMine = use(UpdateMine.class);
                final List<Entity> contacts = contactMine.getContacts(thing);
                final List<Entity> updates = updateMine.updatesOf(thing);

                if (contacts != null) {
                    this.contacts = new EntityListView(as, contacts, EarthView.SHALLOW).asList();
                } else {
                    this.contacts = null;
                }


                if (updates != null) {
                    this.updates = new EntityListView(as, updates, EarthView.SHALLOW).asList();
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
