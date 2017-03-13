package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.ContactEditor;
import com.queatz.snappy.logic.eventables.NewContactEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("contact - empty route");
            case 1:
                EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

                return new EarthViewer(as).getViewForEntityOrThrow(thing).toJson();
            default:
                throw new NothingLogicResponse("contact - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        EarthStore earthStore = new EarthStore(as);

        switch (as.getRoute().size()) {
            case 0: {
                EarthThing thing = earthStore.get(as.getParameters().get(Config.PARAM_THING)[0]);
                EarthThing person = earthStore.get(as.getParameters().get(Config.PARAM_PERSON)[0]);
                String role = extract(as.getParameters().get(Config.PARAM_ROLE));

                EarthThing contact;

                if (role != null) {
                    contact = new ContactEditor(as).newContact(thing, person, role);
                } else {
                    contact = new ContactEditor(as).newContact(thing, person);
                }

                new EarthUpdate(as).send(new NewContactEvent(as.getUser(), contact)).to(person);

                return new EarthViewer(as).getViewForEntityOrThrow(contact).toJson();
            }
            case 1: {
                EarthThing thing = earthStore.get(as.getRoute().get(0));

                // todo edit role

                return new EarthViewer(as).getViewForEntityOrThrow(thing).toJson();
            }

            case 2: {
                if (Config.PATH_DELETE.equals(as.getRoute().get(1))) {
                    EarthThing thing = earthStore.get(as.getRoute().get(0));
                    earthStore.conclude(thing);
                    return new SuccessView(true).toJson();
                }

                throw new NothingLogicResponse("thing - bad path");
            }
        }

        return null;
    }

    private String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }
}
