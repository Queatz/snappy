package com.queatz.snappy.logic.interfaces;

import com.google.gson.JsonObject;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthQueries;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.earth.FrozenQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.email.EarthEmail;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.shared.WebsiteHelper;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.view.SuccessView;
import com.village.things.ClearNotificationEvent;
import com.village.things.DeviceEditor;
import com.village.things.PersonEditor;
import com.village.things.PersonMine;
import com.vlllage.graph.EarthGraph;

import org.apache.commons.lang3.StringUtils;

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

                String select = as.getParameters().containsKey(Config.PARAM_SELECT) ? as.getParameters().get(Config.PARAM_SELECT)[0] : null;
                FrozenQuery query = as.s(EarthQueries.class).byId(as.getUser().key().name());

                return as.s(EarthJson.class).toJson(
                        as.s(EarthGraph.class).queryOne(query.getEarthQuery(), select, query.getVars())
                );
            case 2:
                as.requireUser();

                switch (as.getRoute().get(1)) {
                    case Config.PATH_MESSAGES:
                        return getMessages(as);
                    case Config.PATH_CLUBS:
                        return getClubs(as);
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
                String linkPrecheck = as.getRequest().getParameter(Config.PARAM_LINK_PRECHECK);

                if (linkPrecheck != null) {
                    linkPrecheck = sanitizeUrl(linkPrecheck);

                    if (StringUtils.isEmpty(linkPrecheck) || WebsiteHelper.isReservedUrl(linkPrecheck)) {
                        return new SuccessView(false).toJson();
                    }

                    return new SuccessView(new PersonMine(new EarthAs()).byGoogleUrl(linkPrecheck) == null).toJson();
                }

                String about = as.getRequest().getParameter(Config.PARAM_ABOUT);

                if (about != null) {
                    as.s(PersonEditor.class).updateAbout(as.getUser(), about);
                    return new SuccessView(true).toJson();
                }

                String link = as.getRequest().getParameter(Config.PARAM_LINK);

                if (link != null) {
                    return new SuccessView(as.s(PersonEditor.class)
                            .updateLink(as.getUser(), link)).toJson();
                }

                throw new NothingLogicResponse("me - no params");
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

    private String sanitizeUrl(String link) {
        link = link.trim().toLowerCase();

        if (link.matches(".*[^a-z\\d_].*")) {
            return null;
        }

        return link;
    }

    private String postInfo(EarthAs as) {
        as.requireUser();

        String latitudeParam = as.getParameters().get(Config.PARAM_LATITUDE)[0];
        String longitudeParam = as.getParameters().get(Config.PARAM_LONGITUDE)[0];
        double latitude = Double.valueOf(latitudeParam);
        double longitude = Double.valueOf(longitudeParam);
        final EarthGeo latLng = EarthGeo.of(latitude, longitude);

        as.s(PersonEditor.class).updateLocation(as.getUser(), latLng);

        return new SuccessView(true).toJson();
    }

    private String postReport(EarthAs as, String personId) {
        as.requireUser();

        EarthThing jacob = as.s(EarthStore.class).get(Config.JACOB);
        EarthThing person = as.s(EarthStore.class).get(personId);
        String feedback = as.getRequest().getParameter(Config.PARAM_MESSAGE);

        String report = "reported person with email " +
                person.getString(EarthField.EMAIL) +
                " with the following message:<br /><br />";

        new EarthEmail().sendRawEmail(as.getUser(), jacob, "Village person reported", report + feedback);

        return new SuccessView(true).toJson();
    }

    private String getMessages(EarthAs as) {
        as.requireUser();

        String select = as.getParameters().containsKey(Config.PARAM_SELECT) ? as.getParameters().get(Config.PARAM_SELECT)[0] : null;

        FrozenQuery messagesQuery = as.s(EarthQueries.class).messagesFromOrTo(as.getUser().key().name());
        FrozenQuery contactsQuery = as.s(EarthQueries.class).recentsFor(as.getUser().key().name());

        JsonObject result = new JsonObject();

        result.add("messages", as.s(EarthGraph.class).query(messagesQuery.getEarthQuery(), select, messagesQuery.getVars()));
        result.add("contacts", as.s(EarthGraph.class).query(contactsQuery.getEarthQuery(), select, contactsQuery.getVars()));

        return as.s(EarthJson.class).toJson(result);
    }

    private String getClubs(EarthAs as) {
        as.requireUser();

        String select = as.getParameters().containsKey(Config.PARAM_SELECT) ? as.getParameters().get(Config.PARAM_SELECT)[0] : null;
        FrozenQuery query = as.s(EarthQueries.class).clubsOf(as.getUser());

        return as.s(EarthJson.class).toJson(
                as.s(EarthGraph.class).query(query.getEarthQuery(), select, query.getVars())
        );
    }

    private String postClearNotification(EarthAs as, String notification) {
        as.requireUser();

        as.s(EarthUpdate.class).send(new ClearNotificationEvent(notification))
                .to(as.getUser());

        return new SuccessView(true).toJson();
    }

    private String postUnregisterDevice(EarthAs as, String deviceId) {
        if (deviceId != null && deviceId.length() > 0) {
            as.s(DeviceEditor.class).removeFor(as.getUser().key().name(), deviceId);
            return new SuccessView(true).toJson();
        } else {
            return new SuccessView(false).toJson();
        }
    }

    private String postRegisterDevice(EarthAs as, String deviceId, String socialMode) {
        as.requireUser();

        if (deviceId != null && deviceId.length() > 0) {
            as.s(DeviceEditor.class).newDevice(as.getUser().key().name(), deviceId, socialMode);
            return new SuccessView(true).toJson();
        } else {
            return new SuccessView(false).toJson();
        }
    }
}
