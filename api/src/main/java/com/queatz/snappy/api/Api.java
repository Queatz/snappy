package com.queatz.snappy.api;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.exceptions.Error;
import com.queatz.snappy.exceptions.PrintingError;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 11/17/14.
 */
public class Api {
    private static Api _service;

    public static Api getService() {
        if(_service == null)
            _service = new Api();

        return _service;
    }

    private HashMap<String, Class<? extends Path>> paths;

    public Api() {
        paths = new HashMap<>();
    }

    public void register(final String path, final Class<? extends Path> clazz) {
        if (paths.containsKey(path)) {
            throw new RuntimeException("Cannot re-register path: " + path + " class: " + clazz);
        }

        paths.put(path, clazz);
    }

    public void call(EarthThing user, RequestMethod method, HttpServletRequest request, HttpServletResponse response) {
        String[] path;

        try {
            path = URLDecoder.decode(request.getRequestURI(), "UTF-8").split("/");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if(path.length < 3) {
            throw new PrintingError(Error.NOT_AUTHENTICATED, "bad request length");
        }

        if(!Config.PATH_API.equals(path[1])) {
            throw new PrintingError(Error.NOT_AUTHENTICATED, "bad request path");
        }

        String basePath = path[2];

        ArrayList<String> pathParts = new ArrayList<>();

        pathParts.addAll(Arrays.asList(path).subList(3, path.length));

        if (!paths.containsKey(basePath)) {
            throw new PrintingError(Error.NOT_AUTHENTICATED, "bad request path");
        }

        try {
            Path p = paths.get(basePath).getDeclaredConstructor(Api.class).newInstance(this);

            p._call(pathParts, user, method, request, response);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | IOException e) {
            e.printStackTrace();
            throw new PrintingError(Error.SERVER_ERROR, "api error - " + ((InvocationTargetException) e).getTargetException());
        }
    }
}
