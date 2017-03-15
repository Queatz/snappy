package com.queatz.snappy.service;

import com.google.gson.JsonObject;
import com.queatz.snappy.backend.HttpUtil;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.mines.PersonMine;
import com.queatz.snappy.shared.Config;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by jacob on 11/17/14.
 */
public class Auth {
    private class PersonData {
        String googleUrl;
        String gender;
        String language;
        String firstName;
        String lastName;
        String imageUrl;
        String about;
        String googleId;
    }

    private final PersonMine personMine;
    private final PersonEditor personEditor;
    private final EarthJson earthJson;

    public Auth() {
        EarthAs as = new EarthAs();
        personMine = new PersonMine(as);
        personEditor = new PersonEditor(as);
        earthJson = new EarthJson();
    }

    public boolean isRealGoogleAuth(String email, String token) throws PrintingError {
        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(token))
            return false;

        try {
            String url = Config.GOOGLE_PLUS_TOKENINFO_URL + "?access_token=" + token;
            JsonObject response = earthJson.fromJson(HttpUtil.get(url), JsonObject.class);

            if(response.has("email") && response.get("email").getAsString().equals(email)) {
                return true;
            } else {
                throw new PrintingError(Api.Error.SERVER_ERROR, "no email correctness - " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "real check server fail");
        }
    }

    public PersonData getPersonData(String token) throws PrintingError {
        try {
            String url = Config.GOOGLE_PLUS_PROFILE_URL + "?access_token=" + token;
            JsonObject response = earthJson.fromJson(HttpUtil.get(url), JsonObject.class);

            PersonData personSpec = new PersonData();

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

    public EarthThing fetchUserFromAuth(String email, String token) throws PrintingError {
        if(token == null) {
            return null;
        }

        EarthThing person;

        if(email != null) // Google login
            person = personMine.byEmail(email);
        else // Auth token login
            person = personMine.byToken(token);

        if(person != null && email == null) {
            if (!person.has(EarthField.TOKEN)) {
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "null tok");
            }

            if (person.getString(EarthField.TOKEN).equals(token)) {
                return person;
            }
        }

        if (!isRealGoogleAuth(email, token))
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "iz not realz auth");

        PersonData personData = getPersonData(token);

        if(personData == null) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "no data or google's api changed");
        }

        if (person == null) {
            person = personEditor.newPerson(email);
        }

        person = personEditor.updatePerson(person,
                token,
                personData.firstName,
                personData.lastName,
                personData.gender,
                personData.language,
                personData.imageUrl,
                personData.googleId,
                personData.googleUrl,
                personData.about,
                null);

        return person;
    }
}