package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class OfferView extends ThingView {
    final Integer price;
    final String unit;
    final PersonView person;
    final Date date;
    final int likers;

    public OfferView(EarthAs as, Entity offer) {
        this(as, offer, EarthView.DEEP);
    }

    public OfferView(EarthAs as, Entity offer, EarthView view) {
        super(as, offer, view);

        EarthStore earthStore = use(EarthStore.class);

        price = offer.isNull(EarthField.PRICE) ? null : (int) offer.getLong(EarthField.PRICE);
        unit = offer.getString(EarthField.UNIT);
        person = new PersonView(as, earthStore.get(offer.getKey(EarthField.SOURCE)), EarthView.SHALLOW);
        likers = earthStore.count(EarthKind.LIKE_KIND, EarthField.TARGET, offer.key());
        date = offer.getDateTime(EarthField.CREATED_ON).toDate();
    }
}
