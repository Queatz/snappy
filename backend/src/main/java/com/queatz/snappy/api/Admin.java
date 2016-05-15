package com.queatz.snappy.api;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.mines.PersonMine;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by jacob on 4/11/15.
 */
public class Admin extends Api.Path {

    PersonMine personMine = EarthSingleton.of(PersonMine.class);
    PersonEditor personEditor = EarthSingleton.of(PersonEditor.class);

    public Admin(Api api) {
        super(api);
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

    private void getBetatester(String personEmail) {
        Entity person = personMine.byEmail(personEmail);

        if (person != null) {
            if (StringUtils.isBlank(person.getString(EarthField.SUBSCRIPTION))) {
                personEditor.updateSubscription(person, Config.HOSTING_BETATESTER);
                Push.getService().send(person.key().name(), new PushSpec(Config.PUSH_ACTION_REFRESH_ME));
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
                Push.getService().send(person.key().name(), new PushSpec(Config.PUSH_ACTION_REFRESH_ME));
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
                Push.getService().send(person.key().name(), new PushSpec(Config.PUSH_ACTION_REFRESH_ME));
                ok(person.getString(EarthField.EMAIL) + " can no longer host");
            }
        }
    }
}
