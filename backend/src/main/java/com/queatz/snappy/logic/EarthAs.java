package com.queatz.snappy.logic;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.things.PersonSpec;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class holds the context of a user looking at things.
 */
public class EarthAs {
    private final Api api;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final List<String> route;
    private final Entity user;

    public EarthAs(Api api, HttpServletRequest request, HttpServletResponse response, List<String> route, Entity user) {
        this.api = api;
        this.request = request;
        this.response = response;
        this.route = route;
        this.user = user;
    }

    public Api getApi() {
        return api;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public List<String> getRoute() {
        return route;
    }

    public Entity getUser() {
        return user;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String[]> getParameters() {
        return request.getParameterMap();
    }
}
