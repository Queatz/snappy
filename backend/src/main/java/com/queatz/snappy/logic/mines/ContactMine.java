package com.queatz.snappy.logic.mines;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
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
