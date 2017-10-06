package com.queatz.snappy.api;

import com.queatz.earth.EarthThing;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 10/6/17.
 */
public abstract class Path {
    protected ArrayList<String> path;
    protected EarthThing user;
    protected RequestMethod method;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    public Api api;

    Path(Api api) {
        this.api = api;
    }

    void _call(ArrayList<String> path,
               EarthThing user,
               RequestMethod method,
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
