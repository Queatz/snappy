package com.queatz.snappy;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Auth;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.ErrorResponseSpec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Required parameters:
 *     auth=email;token
 *
 */

public class SnappyServlet extends HttpServlet {
    public SnappyServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(HTTPMethod.GET, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(HTTPMethod.POST, req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(HTTPMethod.PUT, req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(HTTPMethod.DELETE, req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addMainHeaders(resp);
    }

    private void addMainHeaders(final HttpServletResponse resp) {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }

    private void handle(HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addMainHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        try {
            Entity user = Auth.getService().fetchUserFromAuth(req.getParameter(Config.PARAM_EMAIL), req.getParameter(Config.PARAM_AUTH));

            if (user == null) {
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "null auth");
            }

            Api.getService().call(user, method, req, resp);
        } catch (PrintingError e) {
            e.printStackTrace();
            errorOut(resp, e);
        }
    }


    public void errorOut(HttpServletResponse resp, PrintingError error) {
        switch (error.getError()) {
            case NOT_AUTHENTICATED:
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                break;
            case NOT_FOUND:
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                break;
            case NOT_IMPLEMENTED:
                resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                break;
            case SERVER_ERROR:
            default:
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        try {
            resp.getWriter().write(error.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}