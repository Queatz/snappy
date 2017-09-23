package com.queatz.snappy.logic.interfaces;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.AppStore;
import com.queatz.snappy.logic.AppStoreField;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 9/22/17.
 */

public class AppInterface implements Interfaceable {
    @Override
    public String get(EarthAs as) {
        as.requireUser();

        switch (as.getRoute().size()) {
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_TOKEN:
                        return getAppToken(as);
                }
                // Fall-through
            default:
                throw new NothingLogicResponse("app - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        throw new NothingLogicResponse("app - bad method");
    }

    private String getAppToken(EarthAs as) {
        String domain = as.getRequest().getParameter(Config.PARAM_DOMAIN);

        if (Strings.isNullOrEmpty(domain)) {
            throw new NothingLogicResponse("app - no domain");
        }

        return new EarthJson().toJson(ImmutableMap.of(
                AppStoreField.TOKEN, as.s(AppStore.class).tokenForDomain(domain)
        ));
    }
}
