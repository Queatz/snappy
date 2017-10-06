package com.queatz.snappy.service;

import com.image.SnappyImage;
import com.queatz.snappy.SnappyServlet;
import com.queatz.snappy.api.Admin;
import com.queatz.snappy.api.Logic;
import com.queatz.snappy.api.Pirate;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.files.SnappyFiles;
import com.queatz.earth.EarthThing;
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

    public enum Error {
        NOT_FOUND,
        NOT_IMPLEMENTED,
        NOT_AUTHENTICATED,
        SERVER_ERROR
    }

    public abstract static class Path {
        protected ArrayList<String> path;
        protected EarthThing user;
        protected SnappyServlet.RequestMethod method;
        protected HttpServletRequest request;
        protected HttpServletResponse response;

        public Api api;

        public Path(Api api) {
            this.api = api;
        }

        private void _call(ArrayList<String> path,
                           EarthThing user,
                           SnappyServlet.RequestMethod method,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException, PrintingError {
            this.path = path;
            this.user = user;
            this.method = method;
            this.request = request;
            this.response = response;

            call();
        }

        public abstract void call() throws IOException;
    }

    public SnappyServlet snappy;

    public SnappyImage snappyImage;
    public SnappyFiles snappyFiles;

    private HashMap<String, Class<? extends Api.Path>> paths;

    public Api() {
        paths = new HashMap<>();
        paths.put(Config.PATH_EARTH, Logic.class);
        paths.put(Config.PATH_PIRATE, Pirate.class);
        paths.put(Config.PATH_ADMIN, Admin.class);

        snappyImage = new SnappyImage();
        snappyFiles = new SnappyFiles();
    }

    public void call(EarthThing user, SnappyServlet.RequestMethod method, HttpServletRequest request, HttpServletResponse response) {
        String[] path;

        try {
            path = URLDecoder.decode(request.getRequestURI(), "UTF-8").split("/");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if(path.length < 3) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bad request length");
        }

        if(!Config.PATH_API.equals(path[1])) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bad request path");
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
            throw new PrintingError(Api.Error.SERVER_ERROR, "api error - " + ((InvocationTargetException) e).getTargetException());
        }
    }
}
