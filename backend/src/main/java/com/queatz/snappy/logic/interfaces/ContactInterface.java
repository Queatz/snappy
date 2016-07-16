package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.ContactEditor;
import com.queatz.snappy.logic.eventables.NewContactEvent;
import com.queatz.snappy.logic.eventables.NewThingEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactInterface implements Interfaceable {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    private final ContactEditor contactEditor = EarthSingleton.of(ContactEditor.class);
    private final EarthViewer earthViewer = EarthSingleton.of(EarthViewer.class);
    private final EarthUpdate earthUpdate = EarthSingleton.of(EarthUpdate.class);

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("contact - empty route");
            case 1:
                Entity thing = earthStore.get(as.getRoute().get(0));

                return earthViewer.getViewForEntityOrThrow(thing).toJson();
            default:
                throw new NothingLogicResponse("contact - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0: {
                Entity thing = earthStore.get(as.getParameters().get(Config.PARAM_THING)[0]);
                Entity person = earthStore.get(as.getParameters().get(Config.PARAM_PERSON)[0]);
                String role = extract(as.getParameters().get(Config.PARAM_ROLE));

                Entity contact;

                if (role != null) {
                    contact = contactEditor.newContact(thing, person, role);
                } else {
                    contact = contactEditor.newContact(thing, person);
                }

                earthUpdate.send(new NewContactEvent(as.getUser(), contact)).to(person);

                return earthViewer.getViewForEntityOrThrow(contact).toJson();
            }
            case 1: {
                Entity thing = earthStore.get(as.getRoute().get(0));

                // todo edit role

                return earthViewer.getViewForEntityOrThrow(thing).toJson();
            }

            case 2: {
                if (Config.PATH_DELETE.equals(as.getRoute().get(1))) {
                    Entity thing = earthStore.get(as.getRoute().get(0));
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
