package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.GooglePurchaseDataSpec;
import com.queatz.snappy.backend.Json;

/**
 * Created by jacob on 3/28/15.
 */
public class Buy {
    public GooglePurchaseDataSpec makeOrUpdate(GooglePurchaseDataSpec data, String subscriptionInfo) {
        if (data == null) {
            data = new GooglePurchaseDataSpec();
        }

        GooglePurchaseDataSpec subscription;

        subscription = Json.from(subscriptionInfo, GooglePurchaseDataSpec.class);

        GooglePurchaseDataSpec purchase = Datastore.get(GooglePurchaseDataSpec.class).filter("orderId", data.orderId).first().now();

        if(purchase != null) {
            purchase.startTimeMillis = data.startTimeMillis;
            purchase.expiryTimeMillis = data.expiryTimeMillis;
            purchase.autoRenewing = data.autoRenewing;
        }
        else {
            purchase = subscription;
        }

        Datastore.save(purchase);

        return purchase;
    }
}
