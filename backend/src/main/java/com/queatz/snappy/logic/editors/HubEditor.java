package com.queatz.snappy.logic.editors;

import com.google.gcloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 4/4/16.
 */
public class HubEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newHub(String name, String about) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.HUB_KIND))
                .set(EarthField.PHOTO, false)
                .set(EarthField.NAME, name)
                .set(EarthField.ABOUT, about));
    }
}
