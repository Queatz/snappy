package com.queatz.snappy.api;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.shared.Config;

import org.jetbrains.annotations.NotNull;

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
    private final EarthThing user;
    private final boolean internal;

    private Map<Class, Object> singletons;

    public EarthAs(Api api, HttpServletRequest request, HttpServletResponse response, List<String> route, EarthThing user) {
        this(api, request, response, route, user, false);
    }

    public EarthAs(Api api, HttpServletRequest request, HttpServletResponse response, List<String> route, EarthThing user, boolean internal) {
        this.api = api;
        this.request = request;
        this.response = response;
        this.route = route;
        this.user = user;
        this.internal = internal;

        singletons = new HashMap<>();
    }

    public EarthAs () {
        this(null, null, null, null, null, true);
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

    @NotNull
    public EarthThing getUser() {
        requireUser();
        return user;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String[]> getParameters() {
        return request.getParameterMap();
    }

    public boolean hasUser() {
        return user != null;
    }

    public void requireUser() {
        if (!hasUser()) {
            throw new PrintingError(Error.NOT_AUTHENTICATED, "null auth");
        }
    }

    /**
     * Gives permissions to internal calls
     */
    public boolean isInternalCall() {
        return api == null;
    }

    public boolean isInternal() {
        return internal;
    }
}
