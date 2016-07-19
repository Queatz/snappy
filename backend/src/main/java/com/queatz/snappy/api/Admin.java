package com.queatz.snappy.api;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.eventables.RefreshMeEvent;
import com.queatz.snappy.logic.mines.PersonMine;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by jacob on 4/11/15.
 */
public class Admin extends Api.Path {

    private final PersonMine personMine;
    private final PersonEditor personEditor;
    private final EarthUpdate earthUpdate;

    public Admin(Api api) {
        super(api);

        EarthAs as = new EarthAs(api, request, response, path, user);
        personMine = new PersonMine(as);
        personEditor = new PersonEditor(as);
        earthUpdate = new EarthUpdate(as);
    }

    @Override
    public void call() throws IOException {
        switch (method) {
            case GET:
                if (path.size() == 2) {
                    String personEmail = path.get(1);

                    switch (path.get(0)) {
                        case Config.HOSTING_BETATESTER:
                            getBetatester(personEmail);
                            break;
                        case "enable_hosting":
                            getEnableHosting(personEmail);
                            break;
                        case "disable_hosting":
                            getDisableHosting(personEmail);
                            break;
                    }
                }
                break;
        }
    }

    private void ok(String string) {
        try {
            response.getWriter().write(string);
        } catch (IOException e) {
            throw new PrintingError(Api.Error.SERVER_ERROR, e.toString());
        }
    }

    private void getBetatester(String personEmail) {
        Entity person = personMine.byEmail(personEmail);

        if (person != null) {
            if (StringUtils.isBlank(person.getString(EarthField.SUBSCRIPTION))) {
                personEditor.updateSubscription(person, Config.HOSTING_BETATESTER);
                earthUpdate.send(new RefreshMeEvent()).to(person);
                ok(person.getString(EarthField.EMAIL) + " has been upgraded");
            } else {
                ok(person.getString(EarthField.EMAIL) + " is already upgraded");
            }
        }
    }

    private void getEnableHosting(String personEmail) {
        Entity person = personMine.byEmail(personEmail);

        if (person != null) {
            if (StringUtils.isBlank(person.getString(EarthField.SUBSCRIPTION))) {
                personEditor.updateSubscription(person, Config.HOSTING_ENABLED_AVAILABLE);
                earthUpdate.send(new RefreshMeEvent()).to(person);
                ok(person.getString(EarthField.EMAIL) + " can now host");
            } else {
                ok(person.getString(EarthField.EMAIL) + " can already host");
            }
        }
    }

    private void getDisableHosting(String personEmail) {
        Entity person = personMine.byEmail(personEmail);

        if (person != null) {
            if (StringUtils.isBlank(person.getString(EarthField.SUBSCRIPTION))) {
                ok(person.getString(EarthField.EMAIL) + " already can't host");
            } else {
                personEditor.updateSubscription(person, "");
                earthUpdate.send(new RefreshMeEvent()).to(person);
                ok(person.getString(EarthField.EMAIL) + " can no longer host");
            }
        }
    }
}
