package com.queatz.snappy.service;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.queatz.snappy.SnappyServlet;
import com.queatz.snappy.api.*;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;

import java.io.IOException;
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
        protected String user;
        protected HTTPMethod method;
        protected HttpServletRequest request;
        protected HttpServletResponse response;

        public Api api;

        public Path(Api api) {
            this.api = api;
        }

        private void _call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException, PrintingError {
            this.path = path;
            this.user = user;
            this.method = method;
            this.request = request;
            this.response = response;

            call();
        }

        public abstract void call() throws IOException, PrintingError;

        final public void die(String reason) throws PrintingError {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, reason);
        }

        final public void error(String reason) throws PrintingError {
            throw new PrintingError(Error.SERVER_ERROR, reason);
        }

        final public void notFound() throws PrintingError {
            throw new PrintingError(Error.NOT_FOUND);
        }

        final public void handOff(Api.Path to) throws IOException, PrintingError {
            if (path.isEmpty()) {
                error("cannot hand off");
            }

            path.remove(0);

            to._call(path, user, method, request, response);
        }
    }

    public SnappyServlet snappy;

    public GcsService mGCS;
    public AppIdentityService mAppIdentityService;
    public ImagesService mImagesService;

    private HashMap<String, Path> paths;

    public Api() {
        paths = new HashMap<>();
        paths.put("example", new Example(this));
        paths.put(Config.PATH_PARTY, new Party(this));
        paths.put(Config.PATH_MESSAGES, new Messages(this));
        paths.put(Config.PATH_PEOPLE, new People(this));
        paths.put(Config.PATH_FOLLOW, new Follow(this));
        paths.put(Config.PATH_ME, new Me(this));
        paths.put(Config.PATH_PIRATE, new Pirate(this));
        paths.put(Config.PATH_JOIN, new Join(this));
        paths.put(Config.PATH_ADMIN, new Admin(this));
        paths.put(Config.PATH_LOCATIONS, new Locations(this));
        paths.put(Config.PATH_LOCATION, new Location(this));
        paths.put(Config.PATH_HERE, new Here(this));
        paths.put(Config.PATH_BOUNTIES, new Bounties(this));
        paths.put(Config.PATH_BOUNTY, new Bounty(this));
        paths.put(Config.PATH_UPDATE, new Update(this));
        paths.put(Config.PATH_PARTIES, new Parties(this));
        paths.put(Config.PATH_QUEST, new Quest(this));

        mGCS = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        mAppIdentityService = AppIdentityServiceFactory.getAppIdentityService();
        mImagesService = ImagesServiceFactory.getImagesService();
    }

    public void call(String user, HTTPMethod method, HttpServletRequest request, HttpServletResponse response) throws PrintingError {
        String[] path = request.getRequestURI().split("/");

        if(path.length < 3) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bad request length");
        }

        if(!Config.PATH_API.equals(path[1])) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bad request path");
        }

        String basePath = path[2];

        ArrayList<String> pathParts = new ArrayList<>();

        pathParts.addAll(Arrays.asList(path).subList(3, path.length));

        try {
            Path p = paths.get(basePath);

            if(p != null)
                p._call(pathParts, user, method, request, response);
            else
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bad request path");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "api error");
        }
    }
}
