package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Viewable;

/**
 * Created by jacob on 5/8/16.
 */
public class LinkView extends ExistenceView {

    final Viewable source;
    final Viewable target;

    public LinkView(Entity link) {
        this(link, EarthView.DEEP);
    }

    public LinkView(Entity link, EarthView view) {
        super(link, view);

        final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
        final EarthViewer earthViewer = EarthSingleton.of(EarthViewer.class);

        switch (view) {
            case DEEP:
                source = earthViewer.getViewForEntityOrThrow(earthStore.get(link.getKey(EarthField.SOURCE)), view);
                break;
            default:
                source = null;
        }

        target = earthViewer.getViewForEntityOrThrow(earthStore.get(link.getKey(EarthField.TARGET)), view);
    }
}