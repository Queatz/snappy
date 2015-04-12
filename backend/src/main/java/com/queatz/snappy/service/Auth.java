package com.queatz.snappy.service;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
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
import java.util.Iterator;

/**
 * Created by jacob on 11/17/14.
 */
public class Auth {
    private static Auth _service;

    public static Auth getService() {
        if(_service == null)
            _service = new Auth();

        return _service;
    }

    public Auth() {
    }

    public boolean isRealGoogleAuth(String email, String token) throws PrintingError {
        if(email == null || email.isEmpty() || token == null || token.isEmpty())
            return false;

        String s;

        try {
            URL url = new URL(Config.GOOGLE_PLUS_TOKENINFO_URL + "?access_token=" + token);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            s = new String(resp.getContent());
            JSONObject response = new JSONObject(s);

            if(response.getString("email").equals(email)) {
                return true;
            }
        }
        catch (JSONException e) {
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "real check server fail");
        }

        return false;
    }

    public JSONObject getPersonData(JSONObject result, String token) throws PrintingError {
        try {
            URL url = new URL(Config.GOOGLE_PLUS_PROFILE_URL + "?access_token=" + token);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            String s = new String(resp.getContent());
            JSONObject response = new JSONObject(s);

            String gender = "";
            String firstName = "";
            String lastName = "";
            String imageUrl = "";
            String about = "";
            String googleId = "";

            if(response.has("gender"))
                gender = response.getString("gender");

            if(response.has("name")) {
                JSONObject name = response.getJSONObject("name");

                if(name.has("givenName"))
                    firstName = name.getString("givenName");

                if(name.has("familyName"))
                    lastName = name.getString("familyName");
            }

            if(response.has("image") && response.getJSONObject("image").has("url"))
                imageUrl = response.getJSONObject("image").getString("url");

            if(response.has("tagline"))
                about = response.getString("tagline");

            if(response.has("id"))
                googleId = response.getString("id");

            if(result == null)
                result = new JSONObject();

            result.put("gender", gender);
            result.put("firstName", firstName);
            result.put("lastName", lastName);
            result.put("imageUrl", imageUrl);
            result.put("about", about);
            result.put("googleId", googleId);

            return result;
        }
        catch (JSONException e) {
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "user details server fail");
        }
    }

    public String fetchUserFromAuth(String email, String token) throws PrintingError {
        if(token == null) {
            return null;
        }

        JSONObject userJson = new JSONObject();

        Document document = null;

        Results<ScoredDocument> results;

        if(email != null) // Google login
            results = Search.getService().index.get(Search.Type.PERSON).search("email = \"" + email + "\"");
        else // Auth token login
            results = Search.getService().index.get(Search.Type.PERSON).search("token = \"" + token + "\"");

        Iterator<ScoredDocument> resultsIterator = results.iterator();

        if(resultsIterator.hasNext()) {
            document = resultsIterator.next();
        }

        if(document != null && email == null) {
            String tok = document.getOnlyField("token").getAtom();

            if (tok == null) {
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "null tok");
            }

            if (tok.equals(token)) {
                return document.getId();
            }
        }

        if (!isRealGoogleAuth(email, token))
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "iz not realz auth");

        try {
            userJson.put("email", email);
            userJson.put("token", token);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject o = getPersonData(userJson, token);

        if(o == null)
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "no data or google changed");

        Document user = Things.getService().person.createOrUpdateWithJson(document, userJson);

        return user.getId();
    }
}