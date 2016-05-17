package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

import java.util.List;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactMine {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public List<Entity> getContacts(Entity hub) {
        return earthStore.find(EarthKind.CONTACT_KIND, EarthField.SOURCE, hub.key());
    }
}
