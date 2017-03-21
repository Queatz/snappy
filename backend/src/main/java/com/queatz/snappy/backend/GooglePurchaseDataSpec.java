package com.queatz.snappy.backend;

/**
 * @deprecated App was open-sourced.
 *
 * Created by jacob on 10/17/15.
 */
public class GooglePurchaseDataSpec {
    public String orderId;
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
