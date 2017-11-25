package com.queatz.snappy.as;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.exceptions.Error;
import com.queatz.snappy.exceptions.PrintingError;
import com.queatz.snappy.plugins.EarthPlugin;
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
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final List<String> route;
    private final EarthThing user;
    private final boolean internal;

    private Map<Class, Object> singletons;

    public EarthAs(HttpServletRequest request, HttpServletResponse response, List<String> route, EarthThing user) {
        this(request, response, route, user, false);
    }

    public EarthAs(HttpServletRequest request, HttpServletResponse response, List<String> route, EarthThing user, boolean internal) {
        this.request = request;
        this.response = response;
        this.route = route;
        this.user = user;
        this.internal = internal;

        singletons = new HashMap<>();
    }

    public EarthAs () {
        this(null, null, null, null, true);
    }

    @SuppressWarnings("unchecked")
    public <T> T s(Class<T> clazz) {
        if (!singletons.containsKey(clazz)) {
            if (clazz.isInterface()) {
                clazz = EarthPlugin.plugin(clazz);
            }

            try {
                try {
                    singletons.put(clazz, clazz.getConstructor(EarthAs.class).newInstance(this));
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    singletons.put(clazz, clazz.getConstructor().newInstance());
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                Logger.getLogger(Config.NAME).log(Level.SEVERE, "Singleton said nope for " + clazz.getSimpleName(), e);
                throw new RuntimeException(e);
            }
        }

        return (T) singletons.get(clazz);
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

    public boolean isInternal() {
        return internal;
    }
}
