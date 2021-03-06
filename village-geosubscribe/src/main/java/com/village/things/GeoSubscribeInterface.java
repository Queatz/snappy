package com.village.things;

import com.google.common.base.Strings;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.view.SuccessView;

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

                as.s(GeoSubscribeEditor.class).create(latitude, longitude, email, locality);

                return new SuccessView(true).toJson();
            default:
                throw new NothingLogicResponse("geo-subscribe - bad path");
        }
    }

    private String unsubscribe(EarthAs as, String unsubscribeToken) {
        EarthThing geoSubscribe = as.s(GeoSubscribeMine.class).byToken(unsubscribeToken);

        if (geoSubscribe == null) {
            return "This subscription doesn't exist or you've already unsubscribed.";
        }

        as.s(EarthStore.class).conclude(geoSubscribe);

        return "You've been unsubscribed.";
    }
}
