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
import com.queatz.snappy.api.Admin;
import com.queatz.snappy.api.Example;
import com.queatz.snappy.api.Follow;
import com.queatz.snappy.api.Join;
import com.queatz.snappy.api.Location;
import com.queatz.snappy.api.Locations;
import com.queatz.snappy.api.Me;
import com.queatz.snappy.api.Messages;
import com.queatz.snappy.api.Parties;
import com.queatz.snappy.api.Party;
import com.queatz.snappy.api.People;
import com.queatz.snappy.api.Pirate;
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

    public static interface Path {
        public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError;
    }

    public SnappyServlet snappy;

    public GcsService mGCS;
    public AppIdentityService mAppIdentityService;
    public ImagesService mImagesService;

    private HashMap<String, Path> paths;

    public Api() {
        paths = new HashMap<>();
        paths.put("example", new Example(this));
        paths.put(Config.PATH_PARTIES, new Parties(this));
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

        mGCS = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        mAppIdentityService = AppIdentityServiceFactory.getAppIdentityService();
        mImagesService = ImagesServiceFactory.getImagesService();
    }

    public void call(String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws PrintingError {
        String[] path = req.getRequestURI().split("/");

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
                p.call(pathParts, user, method, req, resp);
            else
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bad request path");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "api error");
        }
    }
}
