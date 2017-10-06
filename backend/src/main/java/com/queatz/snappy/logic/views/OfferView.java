package com.queatz.snappy.logic.views;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.view.EarthView;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class OfferView extends CommonThingView {
    final Integer price;
    final String unit;
    final PersonView source;
    final Date date;
    final int likers;
    final Boolean want;

    public OfferView(EarthAs as, EarthThing offer) {
        this(as, offer, EarthView.DEEP);
    }

    public OfferView(EarthAs as, EarthThing offer, EarthView view) {
        super(as, offer, view);

        EarthStore earthStore = use(EarthStore.class);

        price = offer.isNull(EarthField.PRICE) ? null : offer.getNumber(EarthField.PRICE).intValue();
        unit = offer.getString(EarthField.UNIT);
        source = new PersonView(as, earthStore.get(offer.getKey(EarthField.SOURCE)), EarthView.SHALLOW);
        likers = earthStore.count(EarthKind.LIKE_KIND, EarthField.TARGET, offer.key());
        date = offer.getDate(EarthField.CREATED_ON);

        if (offer.has(EarthField.WANT)) {
            want = offer.getBoolean(EarthField.WANT);
        } else {
            want = null;
        }
    }
}
