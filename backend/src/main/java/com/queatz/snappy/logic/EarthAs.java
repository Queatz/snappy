package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    // Per request cache
    final Map<Key, Entity> __entityCache = new HashMap<>();

    private Map<Class, Object> singletons;

    public EarthAs(Api api, HttpServletRequest request, HttpServletResponse response, List<String> route, Entity user) {
        this.api = api;
        this.request = request;
        this.response = response;
        this.route = route;
        this.user = user;

        singletons = new HashMap<>();
    }

    public EarthAs () {
        this(null, null, null, null, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T s(Class<T> clazz) {
        if (!singletons.containsKey(clazz)) {
            try {
                singletons.put(clazz, clazz.getConstructor(EarthAs.class).newInstance(this));
            } catch (InstantiationException e) {
                Logger.getLogger(Config.NAME).log(Level.SEVERE, "Singleton said nope for " + clazz.getSimpleName(), e);
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                Logger.getLogger(Config.NAME).log(Level.SEVERE, "Singleton said nope for " + clazz.getSimpleName(), e);
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                Logger.getLogger(Config.NAME).log(Level.SEVERE, "Singleton said nope for " + clazz.getSimpleName(), e);
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                Logger.getLogger(Config.NAME).log(Level.SEVERE, "Singleton said nope for " + clazz.getSimpleName(), e);
                throw new RuntimeException(e);
            }
        }

        return (T) singletons.get(clazz);
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
