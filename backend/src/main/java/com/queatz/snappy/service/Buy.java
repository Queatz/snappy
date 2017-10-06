package com.queatz.snappy.service;

import com.google.gson.JsonObject;
import com.queatz.snappy.backend.GooglePurchaseDataSpec;
import com.queatz.snappy.util.HttpUtil;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.mines.PersonMine;
import com.queatz.snappy.shared.Config;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 4/4/15.
 */
public class Buy {
    PersonEditor personEditor;
    EarthJson earthJson;
    PersonMine personMine;

    public Buy(EarthAs as) {
        personEditor = new PersonEditor(as);
        earthJson = new EarthJson();
        personMine = new PersonMine(as);
    }

    public String getServerAuthToken() throws PrintingError {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("refresh_token", Config.refreshToken));
            params.add(new BasicNameValuePair("client_id", Config.CLIENT_ID));
            String s = HttpUtil.post(Config.GOOGLE_AUTH_URL, "application/x-www-form-urlencoded; charset=UTF-8", params);

            return earthJson.fromJson(s, JsonObject.class).get("access_token").getAsString();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "verify purchase server fail");
        }
    }

    public String verifyPurchase(GooglePurchaseDataSpec purchaseData) throws PrintingError {
        try {
            String accessToken = getServerAuthToken();

            String url = String.format(Config.GOOGLE_BILLING_URL, Config.PACKAGE, Config.subscriptionProductId, purchaseData.purchaseToken) + "?access_token=" + accessToken;
            return HttpUtil.get(url);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "user purchase server fail");
        }
    }

    public boolean valid(EarthThing me) {
        if(me == null)
            return false;

        final String subscription = me.getString(EarthField.SUBSCRIPTION);
        return subscription != null &&
                !subscription.isEmpty() &&
                !Config.HOSTING_ENABLED_AVAILABLE.equals(subscription);
    }

    public boolean validate(EarthThing me, GooglePurchaseDataSpec purchaseData) throws PrintingError {
        if(me == null)
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought no user");

        if (purchaseData == null) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought");
        }

        String p = verifyPurchase(purchaseData);

        if (p == null) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought 2");
        }

        GooglePurchaseDataSpec data = new com.queatz.snappy.backend.Buy()
                .makeOrUpdate(purchaseData, p);
        String subscription = data == null ? null : data.orderId;

        if (subscription == null)
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought 3");


        long subscribers = personMine.countBySubscription(subscription);

        if(subscribers > 0) {
            throw new PrintingError(Api.Error.NOT_IMPLEMENTED, "not bought already owned by someone else");
        }

        personEditor.updateSubscription(me, subscription);

        return true;
    }
}
