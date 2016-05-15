package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/8/16.
 */
public class OfferEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newOffer(Entity person, String about, Integer price, String unit) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.OFFER_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.ABOUT, about)
                .set(EarthField.PRICE, price)
                .set(EarthField.UNIT, unit));
    }

    public Entity setPhoto(Entity offer, boolean photo) {
        return earthStore.save(earthStore.edit(offer).set(EarthField.PHOTO, photo));
    }
}
