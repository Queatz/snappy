package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.view.EarthView;
import com.queatz.snappy.view.EarthViewer;
import com.queatz.snappy.view.Viewable;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class RecentView extends LinkView {

    final Date updated;
    final boolean seen;
    final Viewable latest;

    public RecentView(EarthAs as, EarthThing recent) {
        this(as, recent, EarthView.DEEP);
    }

    public RecentView(EarthAs as, EarthThing recent, EarthView view) {
        super(as, recent, view);

        final EarthStore earthStore = as.s(EarthStore.class);

        updated = recent.getDate(EarthField.UPDATED_ON);
        seen = recent.getBoolean(EarthField.SEEN);
        latest = use(EarthViewer.class).getViewForEntityOrThrow(earthStore.get(recent.getKey(EarthField.LATEST)));
    }
}
