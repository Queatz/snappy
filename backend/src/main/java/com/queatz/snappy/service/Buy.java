package com.queatz.snappy.service;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.cloud.datastore.Entity;
import com.google.gson.JsonObject;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.GooglePurchaseDataSpec;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.PersonSpec;

import java.io.IOException;
import java.net.URL;

/**
 * Created by jacob on 4/4/15.
 */
public class Buy {
    private static Buy _service;

    public static Buy getService() {
        if(_service == null)
            _service = new Buy();

        return _service;
    }

    PersonEditor personEditor = EarthSingleton.of(PersonEditor.class);
    EarthJson earthJson = EarthSingleton.of(EarthJson.class);

    public Buy() {
    }

    public String getServerAuthToken() throws PrintingError {
        try {
            URL url = new URL(Config.GOOGLE_AUTH_URL);

            String payload = String.format(Config.GOOGLE_AUTH_URL_POST_PARAMS, Config.refreshToken, Config.CLIENT_ID);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.POST);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            httpRequest.addHeader(new HTTPHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
            httpRequest.setPayload(payload.getBytes());
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            String s = new String(resp.getContent(), "UTF-8");

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

            URL url = new URL(String.format(Config.GOOGLE_BILLING_URL, Config.PACKAGE, Config.subscriptionProductId, purchaseData.purchaseToken) + "?access_token=" + accessToken);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            httpRequest.setHeader(new HTTPHeader("Content-Type", "application/json; charset=UTF-8"));
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            return new String(resp.getContent(), "UTF-8");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "user purchase server fail");
        }
    }

    public boolean valid(Entity me) {
        if(me == null)
            return false;

        final String subscription = me.getString(EarthField.SUBSCRIPTION);
        return subscription != null &&
                !subscription.isEmpty() &&
                !Config.HOSTING_ENABLED_AVAILABLE.equals(subscription);
    }

    public boolean validate(Entity me, GooglePurchaseDataSpec purchaseData) throws PrintingError {
        if(me == null)
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought no user");

        if (purchaseData == null) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought");
        }

        String p = verifyPurchase(purchaseData);

        if (p == null) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought 2");
        }

        // XXX Shouldn't really use EarthSingleton here...
        GooglePurchaseDataSpec data = EarthSingleton.of(com.queatz.snappy.backend.Buy.class)
                .makeOrUpdate(purchaseData, p);
        String subscription = data == null ? null : data.orderId;

        if (subscription == null)
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought 3");


        long subscribers = Datastore.get(PersonSpec.class).filter("subscription", subscription).count();

        if(subscribers > 0) {
            throw new PrintingError(Api.Error.NOT_IMPLEMENTED, "not bought already owned by someone else");
        }

        personEditor.updateSubscription(me, subscription);

        return true;
    }
}
