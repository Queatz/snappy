package com.queatz.snappy.api;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.PrintingError;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/19/15.
 */
public class Follow implements Api.Path {
    Api api;

    public Follow(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case POST:

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "follow - bad method");
        }
    }
}
