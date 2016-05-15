package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class RecentView extends LinkView {

    final Date updated;
    final boolean seen;
    final MessageView latest;

    public RecentView(Entity recent) {
        super(recent);

        final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

        updated = recent.getDateTime(EarthField.UPDATED_ON).toDate();
        seen = recent.getBoolean(EarthField.SEEN);
        latest = new MessageView(earthStore.get(recent.getKey(EarthField.LATEST)));
    }
}
