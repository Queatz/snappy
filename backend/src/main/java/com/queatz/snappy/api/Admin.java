package com.queatz.snappy.api;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.PersonSpec;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by jacob on 4/11/15.
 */
public class Admin extends Api.Path {
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
        PersonSpec person = Datastore.get(PersonSpec.class).filter("email", personEmail).first().now();

        if (person != null) {
            if (StringUtils.isBlank(person.subscription)) {
                Thing.getService().person.updateSubscription(person, Config.HOSTING_BETATESTER);
                Push.getService().send(person.id, Util.makeSimplePush(Config.PUSH_ACTION_REFRESH_ME));
                ok(person.email + " has been upgraded");
            } else {
                ok(person.email + " is already upgraded");
            }
        }
    }

    private void getEnableHosting(String personEmail) {
        PersonSpec person = Datastore.get(PersonSpec.class).filter("email", personEmail).first().now();

        if (person != null) {
            if (StringUtils.isBlank(person.subscription)) {
                Thing.getService().person.updateSubscription(person, Config.HOSTING_ENABLED_AVAILABLE);
                Push.getService().send(person.id, Util.makeSimplePush(Config.PUSH_ACTION_REFRESH_ME));
                ok(person.email + " can now host");
            } else {
                ok(person.email + " can already host");
            }
        }
    }

    private void getDisableHosting(String personEmail) {
        PersonSpec person = Datastore.get(PersonSpec.class).filter("email", personEmail).first().now();

        if (person != null) {
            if (StringUtils.isBlank(person.subscription)) {
                ok(person.email + " already can't host");
            } else {
                Thing.getService().person.updateSubscription(person, "");
                Push.getService().send(person.id, Util.makeSimplePush(Config.PUSH_ACTION_REFRESH_ME));
                ok(person.email + " can no longer host");
            }
        }
    }
}
