package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthEmail;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.DeviceEditor;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.eventables.ClearNotificationEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.MessageMine;
import com.queatz.snappy.logic.mines.RecentMine;
import com.queatz.snappy.logic.views.MessagesAndContactsView;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

import java.util.List;

/**
 * Created by jacob on 5/14/16.
 */
public class MeInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        as.requireUser();

        switch (as.getRoute().size()) {
            case 1:
                if (!as.hasUser()) {
                    throw new NothingLogicResponse("sup dude");
                }
                return new EarthViewer(as).getViewForEntityOrThrow(as.getUser(), EarthView.SHALLOW).toJson();
            case 2:
                as.requireUser();

                switch (as.getRoute().get(1)) {
                    case Config.PATH_MESSAGES:
                        return getMessages(as);
                }
                // Fall-through
            default:
                throw new NothingLogicResponse("me - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        as.requireUser();

        switch (as.getRoute().size()) {
            case 1:
                String about = as.getRequest().getParameter(Config.PARAM_ABOUT);

                if (about != null) {
                    new PersonEditor(as).updateAbout(as.getUser(), about);
                }

                return new SuccessView(true).toJson();
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_INFO:
                        return postInfo(as);
                    case Config.PATH_REGISTER_DEVICE:
                        return postRegisterDevice(as,
                                as.getRequest().getParameter(Config.PARAM_DEVICE_ID),
                                as.getRequest().getParameter(Config.PARAM_SOCIAL_MODE)
                        );
                    case Config.PATH_UNREGISTER_DEVICE:
                        return postUnregisterDevice(as, as.getRequest().getParameter(Config.PARAM_DEVICE_ID));
                    case Config.PATH_CLEAR_NOTIFICATION:
                        return postClearNotification(as, as.getRequest().getParameter(Config.PARAM_NOTIFICATION));
                    default:
                        throw new NothingLogicResponse("me - bad path");
                }
            case 3:
                switch (as.getRoute().get(1)){
                    case Config.PATH_REPORT:
                        return postReport(as, as.getRoute().get(2));
                    default:
                        throw new NothingLogicResponse("me - bad path");
                }
            default:
                throw new NothingLogicResponse("me - bad path");
        }
    }

    private String postInfo(EarthAs as) {
        as.requireUser();

        String latitudeParam = as.getParameters().get(Config.PARAM_LATITUDE)[0];
        String longitudeParam = as.getParameters().get(Config.PARAM_LONGITUDE)[0];
        double latitude = Double.valueOf(latitudeParam);
        double longitude = Double.valueOf(longitudeParam);
        final EarthGeo latLng = EarthGeo.of(latitude, longitude);

        new PersonEditor(as).updateLocation(as.getUser(), latLng);

        return new SuccessView(true).toJson();
    }

    private String postReport(EarthAs as, String personId) {
        as.requireUser();

        EarthThing jacob = new EarthStore(as).get(Config.JACOB);
        EarthThing person = new EarthStore(as).get(personId);
        String feedback = as.getRequest().getParameter(Config.PARAM_MESSAGE);

        String report = "reported person with email " +
                person.getString(EarthField.EMAIL) +
                " with the following message:<br /><br />";

        new EarthEmail().sendRawEmail(as.getUser(), jacob, "Village person reported", report + feedback);

        return new SuccessView(true).toJson();
    }

    private String getMessages(EarthAs as) {
        as.requireUser();

        List<EarthThing> messages = new MessageMine(as).messagesFromOrTo(as.getUser().key());
        List<EarthThing> contacts = new RecentMine(as).forPerson(as.getUser());

        return new MessagesAndContactsView(as, messages, contacts).toJson();
    }

    private String postClearNotification(EarthAs as, String notification) {
        as.requireUser();

        new EarthUpdate(as).send(new ClearNotificationEvent(notification))
                .to(as.getUser());

        return new SuccessView(true).toJson();
    }

    private String postUnregisterDevice(EarthAs as, String deviceId) {
        if (deviceId != null && deviceId.length() > 0) {
            new DeviceEditor(as).removeFor(as.getUser().key().name(), deviceId);
            return new SuccessView(true).toJson();
        } else {
            return new SuccessView(false).toJson();
        }
    }

    private String postRegisterDevice(EarthAs as, String deviceId, String socialMode) {
        as.requireUser();

        if (deviceId != null && deviceId.length() > 0) {
            new DeviceEditor(as).newDevice(as.getUser().key().name(), deviceId, socialMode);
            return new SuccessView(true).toJson();
        } else {
            return new SuccessView(false).toJson();
        }
    }
}
