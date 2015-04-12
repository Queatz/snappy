package com.queatz.snappy.api;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.backend.PrintingError;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/8/15.
 */

public class Example implements Api.Path {
    Api api;

    public Example(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                resp.getWriter().write("example");

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "example - bad method");
        }
    }
}
