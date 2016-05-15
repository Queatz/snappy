package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Viewable;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class UpdateView extends ThingView {

    final Date date;
    final PersonView person;
    final long likers;
    final String action;
    final Viewable target;

    public UpdateView(Entity update) {
        super(update);

        final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
        final EarthViewer earthViewer = EarthSingleton.of(EarthViewer.class);

        date = update.getDateTime(EarthField.CREATED_ON).toDate();
        person = new PersonView(earthStore.get(update.getKey(EarthField.SOURCE)), EarthView.SHALLOW);
        likers = earthStore.count(EarthKind.LIKE_KIND, EarthField.TARGET, update.key());

        if (update.contains(EarthField.TARGET)) {
            target = earthViewer.getViewForEntityOrThrow(earthStore.get(update.getKey(EarthField.TARGET)));
        } else {
            target = null;
        }

        if (update.contains(EarthField.ACTION)) {
            action = update.getString(EarthField.ACTION);
        } else {
            action = null;
        }
    }
}