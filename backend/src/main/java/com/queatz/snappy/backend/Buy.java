package com.queatz.snappy.backend;

import com.queatz.snappy.shared.EarthJson;

/**
 * Handles the Android app's purchase states.
 *
 * @deprecated App was open-sourced.
 *
 * Created by jacob on 3/28/15.
 */
public class Buy {

    public GooglePurchaseDataSpec makeOrUpdate(GooglePurchaseDataSpec data, String subscriptionInfo) {
        if (data == null) {
            data = new GooglePurchaseDataSpec();
        }

        GooglePurchaseDataSpec subscription;

        subscription = new EarthJson().fromJson(subscriptionInfo, GooglePurchaseDataSpec.class);

        GooglePurchaseDataSpec purchase = null; // @deprecated

        if(purchase != null) {
            purchase.startTimeMillis = data.startTimeMillis;
            purchase.expiryTimeMillis = data.expiryTimeMillis;
            purchase.autoRenewing = data.autoRenewing;
        }
        else {
            purchase = subscription;
        }

        return purchase;
    }
}
