package com.queatz.snappy.logic.views;

import com.google.appengine.api.datastore.GeoPt;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Viewable;

import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 5/8/16.
 */
public class UpdateView extends ThingView {

    final Date date;
    final PersonView person;
    final long likers;
    final String action;
    final Viewable target;
    final GeoPt geo;
    final List<Viewable> with;
    final List<Viewable> updates;
    final Boolean going;

    public UpdateView(EarthAs as, EarthThing update) {
        this(as, update, EarthView.DEEP);
    }

    public UpdateView(EarthAs as, EarthThing update, EarthView view) {
        super(as, update, view);

        final EarthStore earthStore = use(EarthStore.class);
        final EarthViewer earthViewer = use(EarthViewer.class);

        date = update.getDate(EarthField.CREATED_ON);
        person = new PersonView(as, earthStore.get(update.getKey(EarthField.SOURCE)), EarthView.SHALLOW);
        likers = earthStore.count(EarthKind.LIKE_KIND, EarthField.TARGET, update.key());

        List<EarthThing> joinList = earthStore.find(EarthKind.JOIN_KIND, EarthField.TARGET, update.key());
        with = new EntityListView(as, joinList, EarthView.IDENTITY).asList();

        if (update.has(EarthField.GOING)) {
            going = update.getBoolean(EarthField.GOING);
        } else {
            going = null;
        }

        if (update.has(EarthField.GEO)) {
            geo = new GeoPt(
                    (float) update.getGeo(EarthField.GEO).getLatitude(),
                    (float) update.getGeo(EarthField.GEO).getLongitude()
            );
        } else {
            geo = null;
        }

        if (update.has(EarthField.TARGET)) {
            target = earthViewer.getViewForEntityOrThrow(earthStore.get(update.getKey(EarthField.TARGET)), EarthView.SHALLOW);
        } else {
            target = null;
        }

        if (update.has(EarthField.ACTION)) {
            action = update.getString(EarthField.ACTION);
        } else {
            action = null;
        }

        switch (view) {
            case DEEP:
                List<EarthThing> commentList = earthStore.find(EarthKind.UPDATE_KIND, EarthField.TARGET, update.key());
                updates = new EntityListView(as, commentList, EarthView.SHALLOW).asList();
                break;
            default:
                updates = null;
        }
    }
}
