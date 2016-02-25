package com.queatz.snappy.service;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.JsonObject;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.Json;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.PersonSpec;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

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
        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(token))
            return false;

        String s;

        try {
            URL url = new URL(Config.GOOGLE_PLUS_TOKENINFO_URL + "?access_token=" + token);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            httpRequest.addHeader(new HTTPHeader("Content-Type", "application/json; charset=UTF-8"));
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            s = new String(resp.getContent(), "UTF-8");
            JsonObject response = Json.from(s, JsonObject.class);

            if(response.has("email") && response.get("email").getAsString().equals(email)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "real check server fail");
        }

        throw new PrintingError(Api.Error.SERVER_ERROR, "real check no email correctness");
    }

    public PersonSpec getPersonData(String token) throws PrintingError {
        try {
            URL url = new URL(Config.GOOGLE_PLUS_PROFILE_URL + "?access_token=" + token);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET);
            httpRequest.getFetchOptions().allowTruncate().doNotFollowRedirects();
            httpRequest.addHeader(new HTTPHeader("Content-Type", "application/json; charset=UTF-8"));
            HTTPResponse resp = urlFetchService.fetch(httpRequest);
            String s = new String(resp.getContent(), "UTF-8");
            JsonObject response = Json.from(s, JsonObject.class);

            PersonSpec personSpec = new PersonSpec();

            if(response.has("url") && StringUtils.isNotBlank(response.get("url").getAsString())) {
                personSpec.googleUrl = Util.googleUrl(response.get("url").getAsString()).toLowerCase();
            }

            if(response.has("gender")) {
                personSpec.gender = response.get("gender").getAsString();
            }

            if(response.has("language")) {
                personSpec.language = response.get("language").getAsString();
            }

            if(response.has("name")) {
                JsonObject name = response.get("name").getAsJsonObject();

                if(name.has("givenName")) {
                    personSpec.firstName = name.get("givenName").getAsString();
                }

                if(name.has("familyName")) {
                    personSpec.lastName = name.get("familyName").getAsString();
                }
            }

            if(response.has("image") && response.get("image").getAsJsonObject().has("url")) {
                personSpec.imageUrl = response.get("image").getAsJsonObject().get("url").getAsString();
            }

            if(response.has("tagline")) {
                personSpec.about = response.get("tagline").getAsString();
            }

            if(response.has("id")) {
                personSpec.googleId = response.get("id").getAsString();
            }

            return personSpec;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "user details server fail");
        }
    }

    public PersonSpec fetchUserFromAuth(String email, String token) throws PrintingError {
        if(token == null) {
            return null;
        }

        PersonSpec person;

        if(email != null) // Google login
            person = Datastore.get(PersonSpec.class).filter("email", email).first().now();
        else // Auth token login
            person = Datastore.get(PersonSpec.class).filter("token", token).first().now();

        if(person != null && email == null) {
            if (person.token == null) {
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "null tok");
            }

            if (person.token.equals(token)) {
                person.auth = person.token;
                return person;
            }
        }

        if (!isRealGoogleAuth(email, token))
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "iz not realz auth");

        PersonSpec o = getPersonData(token);

        if(o == null) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "no data or google changed");
        }

        o.email = email;
        o.token = token;

        person = Thing.getService().person.createOrUpdate(person, o);
        person.auth = person.token;
        return person;
    }
}