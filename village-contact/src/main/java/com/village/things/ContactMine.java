package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;

import java.util.List;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactMine extends EarthControl {
    public ContactMine(final EarthAs as) {
        super(as);
    }

    public List<EarthThing> getContacts(EarthThing thing) {
        return use(EarthStore.class).find(EarthKind.CONTACT_KIND, EarthField.SOURCE, thing.key());
    }
}
