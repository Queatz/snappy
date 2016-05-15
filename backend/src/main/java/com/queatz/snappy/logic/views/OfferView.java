package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/8/16.
 */
public class OfferView extends ThingView {
    final Integer price;
    final String unit;
    final PersonView person;
    final int likers;

    public OfferView(Entity offer) {
        super(offer);

        EarthStore earthStore = EarthSingleton.of(EarthStore.class);

        price = offer.isNull(EarthField.PRICE) ? null : (int) offer.getLong(EarthField.PRICE);
        unit = offer.getString(EarthField.UNIT);
        person = new PersonView(earthStore.get(offer.getKey(EarthField.SOURCE)));
        likers = earthStore.count(EarthKind.LIKE_KIND, EarthField.TARGET, offer.key());
    }
}
