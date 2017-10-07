package com.queatz.snappy.api;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.Error;
import com.queatz.snappy.exceptions.PrintingError;
import com.queatz.snappy.events.EarthUpdate;
import com.village.things.PersonEditor;
import com.village.things.RefreshMeEvent;
import com.village.things.PersonMine;
import com.queatz.snappy.shared.Config;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Api endpoints for manipulating Village.
 *
 * Created by jacob on 4/11/15.
 */
public class Admin extends Path {

    private final PersonMine personMine;
    private final PersonEditor personEditor;
    private final EarthUpdate earthUpdate;

    public Admin(Api api) {
        super(api);

        EarthAs as = new EarthAs(request, response, path, user);
        personMine = as.s(PersonMine.class);
        personEditor = as.s(PersonEditor.class);
        earthUpdate = as.s(EarthUpdate.class);
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
            throw new PrintingError(Error.SERVER_ERROR, e.toString());
        }
    }

    private void getBetatester(String personEmail) {
        EarthThing person = personMine.byEmail(personEmail);

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
        EarthThing person = personMine.byEmail(personEmail);

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
        EarthThing person = personMine.byEmail(personEmail);

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
