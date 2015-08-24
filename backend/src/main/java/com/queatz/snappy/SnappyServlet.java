package com.queatz.snappy;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handle(HTTPMethod.GET, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        handle(HTTPMethod.POST, req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        handle(HTTPMethod.PUT, req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        handle(HTTPMethod.DELETE, req, resp);
    }

    private void handle(HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) {
        String user;

        resp.setContentType("text/javascript");

        try {
            user = Auth.getService().fetchUserFromAuth(req.getParameter(Config.PARAM_EMAIL), req.getParameter(Config.PARAM_AUTH));

            if(user == null)
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "null auth");

            Api.getService().call(user, method, req, resp);
        }
        catch (PrintingError e) {
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

        if(false) try {
            JSONObject json = new JSONObject();
            json.put("error", error.toString());
            json.put("reason", error.getReason());

            resp.getWriter().println(json.toString());
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}