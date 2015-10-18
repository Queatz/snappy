package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.OfferSpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.util.Date;

/**
 * Created by jacob on 8/29/15.
 */
public class Offer {
    public OfferSpec create(PersonSpec user, String details, int price) {
        if(price < 0 || price > Config.OFFER_MAX_PRICE)
            return null;

        OfferSpec offer = Datastore.create(OfferSpec.class);
        offer.details = details;
        offer.personId = Datastore.key(user);
        offer.price = price;
        offer.created = new Date();

        Datastore.save(offer);
        return offer;
    }

    public void delete(String offerId) {
        Datastore.delete(OfferSpec.class, offerId);
    }
}
