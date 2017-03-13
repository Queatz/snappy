package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class RecentView extends LinkView {

    final Date updated;
    final boolean seen;
    final MessageView latest;

    public RecentView(EarthAs as, EarthThing recent) {
        this(as, recent, EarthView.DEEP);
    }

    public RecentView(EarthAs as, EarthThing recent, EarthView view) {
        super(as, recent, view);

        final EarthStore earthStore = use(EarthStore.class);

        updated = recent.getDate(EarthField.UPDATED_ON);
        seen = recent.getBoolean(EarthField.SEEN);
        latest = new MessageView(as, earthStore.get(recent.getKey(EarthField.LATEST)));
    }
}
