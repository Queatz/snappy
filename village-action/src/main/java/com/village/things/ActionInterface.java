package com.village.things;

import com.google.common.base.Strings;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.api.StringResponse;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.view.SuccessView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 10/11/17.
 */

public class ActionInterface extends CommonLinkInterface {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                EarthThing action = as.s(EarthStore.class).get(as.getRoute().get(0));

                String value = extract(as.getParameters().get(Config.PARAM_VALUE));
                String token = extract(as.getParameters().get(Config.PARAM_TOKEN));

                // Change value remotely
                if (!Strings.isNullOrEmpty(value) && !Strings.isNullOrEmpty(token)) {
                    if (action.getString(EarthField.TOKEN).equals(token)) {
                        EarthAs ass = new EarthAs();
                        ass.s(ActionEditor.class).setValue(action, value);

                        return new SuccessView(true).toJson();
                    } else {
                        return new SuccessView(false).toJson();
                    }
                }

                break;
        }

        return super.get(as);
    }

    @Override
    public String post(EarthAs as) {
        EarthStore earthStore = as.s(EarthStore.class);

        switch (as.getRoute().size()) {
            case 1:
                EarthThing action = earthStore.get(as.getRoute().get(0));
                String value = extract(as.getParameters().get(Config.PARAM_VALUE));

                // Change value

                if (!Strings.isNullOrEmpty(value)) {
                    EarthThing source = as.s(EarthStore.class).get(action.getString(EarthField.SOURCE));

                    new EarthAs().s(ActionEditor.class).setValue(action, value);

                    if (source != null) {
                        EarthThing user = as.hasUser() ? as.getUser() : null;
                        ActionQueue.getService().enqueue(action, value, user);
                        as.s(EarthUpdate.class).send(new ActionChangeEvent(user, action, value)).to(source);
                        throw new StringResponse(new SuccessView(true).toJson());
                    }

                    return new SuccessView(false).toJson();
                }

                break;
        }

        return super.post(as);
    }

    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role) {
        as.requireUser();

        String data = extract(as.getParameters().get(Config.PARAM_DATA));
        String type = extract(as.getParameters().get(Config.PARAM_TYPE));
        String token = extract(as.getParameters().get(Config.PARAM_TOKEN));

        return as.s(ActionEditor.class).newAction(source, target, role, type, data, token);
    }

    @Override
    public EarthThing edit(EarthAs as, EarthThing action, HttpServletRequest request) {
        String role = extract(as.getParameters().get(Config.PARAM_ROLE));
        String data = extract(as.getParameters().get(Config.PARAM_DATA));
        String type = extract(as.getParameters().get(Config.PARAM_TYPE));
        String token = extract(as.getParameters().get(Config.PARAM_TOKEN));

        if (!(Strings.isNullOrEmpty(role) && Strings.isNullOrEmpty(data) && Strings.isNullOrEmpty(type) && Strings.isNullOrEmpty(token))) {
            return as.s(ActionEditor.class).edit(action, role, type, data, token);
        }

        return action;
    }
}