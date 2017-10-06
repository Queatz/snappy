package com.queatz.snappy.logic.interfaces;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.AppStore;
import com.queatz.snappy.appstore.AppStoreField;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.api.Interfaceable;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;

import org.apache.commons.fileupload.util.Streams;
import org.apache.http.HttpHeaders;

import java.io.IOException;

/**
 * Created by jacob on 9/22/17.
 */

public class AppInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_TOKEN:
                        return getAppToken(as);
                    case Config.PATH_STORE:
                        return getAppStore(as);
                }
                // Fall-through
            default:
                throw new NothingLogicResponse("app - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_STORE:
                        return putAppStore(as);
                }
                // Fall-through
            default:
                throw new NothingLogicResponse("app - bad path");
        }
    }

    private String getAppToken(EarthAs as) {
        as.requireUser();

        String domain = as.getRequest().getParameter(Config.PARAM_DOMAIN);

        if (Strings.isNullOrEmpty(domain)) {
            throw new NothingLogicResponse("app - no domain");
        }

        return new EarthJson().toJson(ImmutableMap.of(
                AppStoreField.TOKEN, as.s(AppStore.class).tokenForDomain(domain)
        ));
    }

    private String getAppStore(EarthAs as) {
        String q = as.getRequest().getParameter(Config.PARAM_Q);
        String appToken = as.getRequest().getHeader(HttpHeaders.AUTHORIZATION);

        if (Strings.isNullOrEmpty(appToken)) {
            throw new NothingLogicResponse("app - missing auth");
        }

        return as.s(AppStore.class).get(appToken, q);
    }

    private String putAppStore(EarthAs as) {
        String q = as.getRequest().getParameter(Config.PARAM_Q);
        String appToken = as.getRequest().getHeader(HttpHeaders.AUTHORIZATION);

        if (Strings.isNullOrEmpty(appToken)) {
            throw new NothingLogicResponse("app - missing auth");
        }

        String v;
        try {
            v = Streams.asString(as.getRequest().getInputStream(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return as.s(AppStore.class).put(appToken, q, v);
    }
}
