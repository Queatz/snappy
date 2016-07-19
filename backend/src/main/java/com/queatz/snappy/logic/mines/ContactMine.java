package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;

import java.util.List;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactMine extends EarthControl {
    public ContactMine(final EarthAs as) {
        super(as);
    }

    public List<Entity> getContacts(Entity thing) {
        return use(EarthStore.class).find(EarthKind.CONTACT_KIND, EarthField.SOURCE, thing.key());
    }
}
