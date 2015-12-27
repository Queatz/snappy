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
import com.queatz.snappy.api.Bounties;
import com.queatz.snappy.api.Bounty;
import com.queatz.snappy.api.Example;
import com.queatz.snappy.api.Follow;
import com.queatz.snappy.api.Here;
import com.queatz.snappy.api.Join;
import com.queatz.snappy.api.Location;
import com.queatz.snappy.api.Locations;
import com.queatz.snappy.api.Me;
import com.queatz.snappy.api.Messages;
import com.queatz.snappy.api.Offer;
import com.queatz.snappy.api.Parties;
import com.queatz.snappy.api.Party;
import com.queatz.snappy.api.People;
import com.queatz.snappy.api.Pirate;
import com.queatz.snappy.api.Quest;
import com.queatz.snappy.api.Update;
import com.queatz.snappy.backend.Json;
import com.queatz.snappy.backend.ObjectResponse;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.SuccessResponseSpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
        protected PersonSpec user;
        protected HTTPMethod method;
        protected HttpServletRequest request;
        protected HttpServletResponse response;

        public Api api;

        public Path(Api api) {
            this.api = api;
        }

        private void _call(ArrayList<String> path, PersonSpec user, HTTPMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException, PrintingError {
            this.path = path;
            this.user = user;
            this.method = method;
            this.request = request;
            this.response = response;

            call();
        }

        public abstract void call() throws IOException;

        final public void die(String reason) {
            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, reason);
        }

        final public void error(String reason) {
            throw new PrintingError(Error.SERVER_ERROR, reason);
        }

        final public void notFound() {
            throw new PrintingError(Error.NOT_FOUND);
        }

        final public void ok(boolean bool) {
            ok(new SuccessResponseSpec(Boolean.toString(bool)));
        }

        final public void ok(String string) {
            ok(new SuccessResponseSpec(string));
        }

        final public void ok(Object object) {
            ok(object, Json.Compression.NONE);
        }

        final public void ok(Object object, Json.Compression compression) {
            if (object == null) {
                notFound();
            }

            throw new ObjectResponse(object, compression);
        }

        final public void handOff(Api.Path to) throws IOException {
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

    private HashMap<String, Class<? extends Api.Path>> paths;

    public Api() {
        paths = new HashMap<>();
        paths.put("example", Example.class);
        paths.put(Config.PATH_PARTY, Party.class);
        paths.put(Config.PATH_MESSAGES, Messages.class);
        paths.put(Config.PATH_PEOPLE, People.class);
        paths.put(Config.PATH_FOLLOW, Follow.class);
        paths.put(Config.PATH_ME, Me.class);
        paths.put(Config.PATH_PIRATE, Pirate.class);
        paths.put(Config.PATH_JOIN, Join.class);
        paths.put(Config.PATH_ADMIN, Admin.class);
        paths.put(Config.PATH_LOCATIONS, Locations.class);
        paths.put(Config.PATH_LOCATION, Location.class);
        paths.put(Config.PATH_HERE, Here.class);
        paths.put(Config.PATH_BOUNTIES, Bounties.class);
        paths.put(Config.PATH_BOUNTY, Bounty.class);
        paths.put(Config.PATH_UPDATE, Update.class);
        paths.put(Config.PATH_PARTIES, Parties.class);
        paths.put(Config.PATH_QUEST, Quest.class);
        paths.put(Config.PATH_OFFER, Offer.class);

        mGCS = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        mAppIdentityService = AppIdentityServiceFactory.getAppIdentityService();
        mImagesService = ImagesServiceFactory.getImagesService();
    }

    public void call(PersonSpec user, HTTPMethod method, HttpServletRequest request, HttpServletResponse response) {
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

        if (!paths.containsKey(basePath)) {
            throw new PrintingError(Error.NOT_AUTHENTICATED, "bad request path");
        }

        try {
            Path p = paths.get(basePath).getDeclaredConstructor(Api.class).newInstance(this);

            p._call(pathParts, user, method, request, response);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.SERVER_ERROR, "api error");
        }
    }
}
