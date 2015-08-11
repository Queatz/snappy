package com.queatz.snappy.service;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;

import org.json.JSONException;
import org.json.JSONObject;

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

    public Buy() {
    }

    public String getServerAuthToken() throws PrintingError {
        try {
            URL url = new URL(Config.GOOGLE_AUTH_URL);

            String payload = String.format(Config.GOOGLE_AUTH_URL_POST_PARAMS, Config.refreshToken, Config.CLIENT_ID);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.POST);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            httpRequest.addHeader(new HTTPHeader("Content-Type", "application/x-www-form-urlencoded"));
            httpRequest.setPayload(payload.getBytes());
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            String s = new String(resp.getContent());

            try {
                return new JSONObject(s).getString("access_token");
            }
            catch (JSONException e) {
                e.printStackTrace();
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "invalid server token info");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "verify purchase server fail");
        }
    }

    public String verifyPurchase(String purchaseData) throws PrintingError {
        try {
            String purchaseToken = new JSONObject(purchaseData).getString("purchaseToken");

            String accessToken = getServerAuthToken();

            URL url = new URL(String.format(Config.GOOGLE_BILLING_URL, Config.PACKAGE, Config.subscriptionProductId, purchaseToken) + "?access_token=" + accessToken);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            String s = new String(resp.getContent());

            return s;
        }
        catch (JSONException e) {
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "user purchase server fail");
        }
    }

    public boolean valid(String user) {
        Document me = Search.getService().get(Search.Type.PERSON, user);

        if(me == null)
            return false;

        try {
            String s = me.getOnlyField("subscription").getAtom();

            return s != null && !s.isEmpty() && !Config.HOSTING_ENABLED_AVAILABLE.equals(s);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validate(String user, String purchaseData) throws PrintingError {
        Document me = Search.getService().get(Search.Type.PERSON, user);

        if(me == null)
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought no user");

        if (purchaseData == null) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought");
        }

        String p = verifyPurchase(purchaseData);

        if (p == null) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought 2");
        }

        Document subscription = Things.getService().buy.makeOrUpdate(purchaseData, p);

        if (subscription == null)
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "not bought 3");


        long subscribers = Search.getService().index.get(Search.Type.PERSON).search("subscription = \"" + subscription.getId() + "\"").getNumberFound();

        if(subscribers > 0) {
            throw new PrintingError(Api.Error.NOT_IMPLEMENTED, "not bought already owned by someone else");
        }

        Things.getService().person.updateSubscription(me, subscription.getId());

        return true;
    }
}
