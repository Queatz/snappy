package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.NullValue;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/8/16.
 */
public class OfferEditor extends EarthControl {
    private final EarthStore earthStore;

    public OfferEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public Entity newOffer(Entity person, String about, boolean want, Integer price, String unit) {
        Entity.Builder edit = earthStore.edit(earthStore.create(EarthKind.OFFER_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.ABOUT, about)
                .set(EarthField.PHOTO, false)
                .set(EarthField.WANT, true)
                .set(EarthField.NAME, NullValue.of())
                .set(EarthField.UNIT, unit);

        if (price == null) {
            edit.set(EarthField.PRICE, NullValue.of());
        } else {
            edit.set(EarthField.PRICE, price);
        }

        return earthStore.save(edit);
    }

    public Entity setPhoto(Entity offer, boolean photo) {
        return earthStore.save(earthStore.edit(offer).set(EarthField.PHOTO, photo));
    }

    public Entity edit(Entity entity, String details, Integer price, String unit) {
        Entity.Builder edit = earthStore.edit(entity)
                .set(EarthField.ABOUT, details)
                .set(EarthField.UNIT, unit);

        if (price == null) {
            edit.set(EarthField.PRICE, NullValue.of());
        } else {
            edit.set(EarthField.PRICE, price);
        }

        return earthStore.save(edit);
    }
}
