package com.queatz.snappy.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by jacob on 10/17/15.
 */
@Entity
public class GooglePurchaseDataSpec {
    public @Id String orderId;
    public String packageName;
    public String productId;
    public long purchaseTime;
    public long purchaseState;
    public String developerPayload;
    public String purchaseToken;

    public long startTimeMillis;
    public long expiryTimeMillis;
    public boolean autoRenewing;
}
