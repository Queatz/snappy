package com.queatz.snappy.logic.views;

import com.queatz.snappy.api.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
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

    public PartyView(EarthAs as, EarthThing party) {
        this(as, party, EarthView.DEEP);
    }

    public PartyView(EarthAs as, EarthThing party, EarthView view) {
        super(as, party, view);

        final EarthStore earthStore = use(EarthStore.class);
        final EarthViewer earthViewer = use(EarthViewer.class);

        date = party.getDate(EarthField.DATE);
        full = party.getBoolean(EarthField.FULL);
        host = earthViewer.getViewForEntityOrThrow(earthStore.get(party.getKey(EarthField.HOST)), EarthView.SHALLOW);
        location = earthViewer.getViewForEntityOrThrow(earthStore.get(party.getKey(EarthField.LOCATION)), EarthView.SHALLOW);

        switch (view) {
            case DEEP:
                List<EarthThing> joinsList = earthStore.find(EarthKind.JOIN_KIND, EarthField.TARGET, party.key());
                joins = new EntityListView(as, joinsList, EarthView.SHALLOW).asList();

                break;
            default:
                joins = null;
        }
    }
}
