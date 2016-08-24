package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Viewable;

import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 5/14/16.
 */
public class PartyView extends ThingView {

    final Date date;
    final List<Viewable> joins;
    final Boolean full;
    final Viewable host;
    final Viewable location;

    public PartyView(EarthAs as, Entity party) {
        this(as, party, EarthView.DEEP);
    }

    public PartyView(EarthAs as, Entity party, EarthView view) {
        super(as, party, view);

        final EarthStore earthStore = use(EarthStore.class);
        final EarthViewer earthViewer = use(EarthViewer.class);

        date = party.getDateTime(EarthField.DATE).toDate();
        full = party.getBoolean(EarthField.FULL);
        host = earthViewer.getViewForEntityOrThrow(earthStore.get(party.getKey(EarthField.HOST)), EarthView.SHALLOW);
        location = earthViewer.getViewForEntityOrThrow(earthStore.get(party.getKey(EarthField.LOCATION)), EarthView.SHALLOW);

        switch (view) {
            case DEEP:
                List<Entity> joinsList = earthStore.find(EarthKind.JOIN_KIND, EarthField.TARGET, party.key());
                joins = new EntityListView(as, joinsList, EarthView.SHALLOW).asList();

                break;
            default:
                joins = null;
        }
    }
}
