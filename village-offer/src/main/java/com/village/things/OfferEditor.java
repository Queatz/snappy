package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

/**
 * Created by jacob on 5/8/16.
 */
public class OfferEditor extends EarthControl {
    private final EarthStore earthStore;

    public OfferEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newOffer(EarthThing person, String about, boolean want, Integer price, String unit) {
        EarthThing.Builder edit = earthStore.edit(earthStore.create(EarthKind.OFFER_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.ABOUT, about)
                .set(EarthField.PHOTO, false)
                .set(EarthField.WANT, want)
                .set(EarthField.NAME)
                .set(EarthField.UNIT, unit);

        if (price == null) {
            edit.set(EarthField.PRICE);
        } else {
            edit.set(EarthField.PRICE, price);
        }

        return earthStore.save(edit);
    }

    public EarthThing setPhoto(EarthThing offer, boolean photo) {
        return earthStore.save(earthStore.edit(offer).set(EarthField.PHOTO, photo));
    }

    public EarthThing edit(EarthThing entity, String details, Integer price, String unit) {
        EarthThing.Builder edit = earthStore.edit(entity)
                .set(EarthField.ABOUT, details)
                .set(EarthField.UNIT, unit);

        if (price == null) {
            edit.set(EarthField.PRICE);
        } else {
            edit.set(EarthField.PRICE, price);
        }

        return earthStore.save(edit);
    }
}
