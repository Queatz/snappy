package com.queatz.snappy.backend;

import com.googlecode.objectify.ObjectifyService;
import com.queatz.snappy.logic.EarthJson;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by jacob on 3/28/15.
 */
public class Buy {

    static {
        ObjectifyService.register(GooglePurchaseDataSpec.class);
    }

    public GooglePurchaseDataSpec makeOrUpdate(GooglePurchaseDataSpec data, String subscriptionInfo) {
        if (data == null) {
            data = new GooglePurchaseDataSpec();
        }

        GooglePurchaseDataSpec subscription;

        subscription = new EarthJson().fromJson(subscriptionInfo, GooglePurchaseDataSpec.class);

        GooglePurchaseDataSpec purchase = ofy().load().type(GooglePurchaseDataSpec.class).filter("orderId", data.orderId).first().now();

        if(purchase != null) {
            purchase.startTimeMillis = data.startTimeMillis;
            purchase.expiryTimeMillis = data.expiryTimeMillis;
            purchase.autoRenewing = data.autoRenewing;
        }
        else {
            purchase = subscription;
        }

        ofy().save().entity(purchase);

        return purchase;
    }
}
