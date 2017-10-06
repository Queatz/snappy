package com.queatz.snappy.logic.interfaces;

import com.google.common.base.Strings;
import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.GeoSubscribeEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.GeoSubscribeMine;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 5/8/17.
 */

public class GeoSubscribeInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {

        switch (as.getRoute().size()) {
            case 1:
                String unsubscribe = as.getRequest().getParameter(Config.PARAM_UNSUBSCRIBE);

                if (!Strings.isNullOrEmpty(unsubscribe)) {
                    return this.unsubscribe(as, unsubscribe);
                }
        }

        throw new NothingLogicResponse("geo-subscribe - bad path");
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                double latitude = Double.parseDouble(as.getRequest().getParameter(Config.PARAM_LATITUDE));
                double longitude = Double.parseDouble(as.getRequest().getParameter(Config.PARAM_LATITUDE));
                String email = as.getRequest().getParameter(Config.PARAM_EMAIL);
                String locality = as.getRequest().getParameter(Config.PARAM_NAME);

                if (Strings.isNullOrEmpty(email)) {
                    throw new NothingLogicResponse("geo-subscribe - no email");
                }

                new GeoSubscribeEditor(as).create(latitude, longitude, email, locality);

                return new SuccessView(true).toJson();
            default:
                throw new NothingLogicResponse("geo-subscribe - bad path");
        }
    }

    private String unsubscribe(EarthAs as, String unsubscribeToken) {
        EarthThing geoSubscribe = new GeoSubscribeMine(as).byToken(unsubscribeToken);

        if (geoSubscribe == null) {
            return "This subscription doesn't exist or you've already unsubscribed.";
        }

        new EarthStore(as).conclude(geoSubscribe);

        return "You've been unsubscribed.";
    }
}
