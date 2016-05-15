package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/8/16.
 */
public class LinkView extends ExistenceView {

    final ExistenceView source;
    final ExistenceView target;

    public LinkView(Entity link) {
        super(link);

        EarthStore earthStore = EarthSingleton.of(EarthStore.class);

        source = new ExistenceView(earthStore.get(link.getKey(EarthField.SOURCE)));
        target = new ExistenceView(earthStore.get(link.getKey(EarthField.TARGET)));
    }
}