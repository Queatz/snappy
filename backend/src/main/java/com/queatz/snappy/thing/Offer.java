package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.OfferSpec;
import com.queatz.snappy.shared.things.PersonSpec;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by jacob on 8/29/15.
 */
public class Offer {
    public OfferSpec create(PersonSpec user, String details, int price, String unit) {

        if (com.queatz.snappy.service.Buy.getService().valid(user)) {
            price = Math.min(Config.PAID_OFFER_PRICE_MAX, Math.max(Config.PAID_OFFER_PRICE_MIN, price));
        } else {
            price = Math.min(Config.FREE_OFFER_PRICE_MAX, Math.max(Config.FREE_OFFER_PRICE_MIN, price));
        }

        if (Math.abs(price) < 200) {
            price = (int) Math.floor(price / 10) * 10;
        } else if (Math.abs(price) < 1000) {
            price = (int) Math.floor(price / 50) * 50;
        } else {
            price = (int) Math.floor(price / 100) * 100;
        }

        OfferSpec offer = Datastore.create(OfferSpec.class);
        offer.details = details;
        offer.personId = Datastore.key(user);
        offer.price = price;
        offer.unit = StringUtils.isBlank(unit) ? null : unit.trim();
        offer.created = new Date();

        Datastore.save(offer);
        return offer;
    }

    public void delete(String offerId) {
        Datastore.delete(OfferSpec.class, offerId);
    }
}
