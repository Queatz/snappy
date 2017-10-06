package com.queatz.snappy.logic.views;

import com.queatz.snappy.api.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Viewable;

/**
 * Created by jacob on 5/8/16.
 */
public class LinkView extends ExistenceView {

    final Viewable source;
    final Viewable target;

    public LinkView(EarthAs as, EarthThing link) {
        this(as, link, EarthView.DEEP);
    }

    public LinkView(EarthAs as, EarthThing link, EarthView view) {
        super(as, link, view);

        final EarthStore earthStore = use(EarthStore.class);
        final EarthViewer earthViewer = use(EarthViewer.class);

        switch (view) {
            case DEEP:
            case SHALLOW:
                target = earthViewer.getViewForEntityOrThrow(earthStore.get(link.getKey(EarthField.TARGET)), view);
                break;
            case IDENTITY:
            default:
                target = null;
        }

        source = earthViewer.getViewForEntityOrThrow(earthStore.get(link.getKey(EarthField.SOURCE)), view);
    }
}