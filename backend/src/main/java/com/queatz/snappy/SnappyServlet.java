package com.queatz.snappy;

import com.queatz.earth.EarthAuthority;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.api.Admin;
import com.queatz.snappy.api.Api;
import com.queatz.snappy.api.Logic;
import com.queatz.snappy.api.Pirate;
import com.queatz.snappy.exceptions.PrintingError;
import com.queatz.snappy.api.RequestMethod;
import com.queatz.snappy.api.StringResponse;
import com.queatz.snappy.events.Queue;
import com.queatz.snappy.images.ImageQueue;
import com.queatz.snappy.logic.authorities.*;
import com.queatz.snappy.service.Auth;
import com.queatz.snappy.shared.Config;

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
        Api.getService().register(Config.PATH_EARTH, Logic.class);
        Api.getService().register(Config.PATH_PIRATE, Pirate.class);
        Api.getService().register(Config.PATH_ADMIN, Admin.class);

        EarthAuthority.register(EarthKind.PERSON_KIND, new PersonAuthority());
        EarthAuthority.register(EarthKind.MESSAGE_KIND, new MessageAuthority());
        EarthAuthority.register(EarthKind.HUB_KIND, new HubAuthority());
        EarthAuthority.register(EarthKind.PROJECT_KIND, new ProjectAuthority());
        EarthAuthority.register(EarthKind.RESOURCE_KIND, new ResourceAuthority());
        EarthAuthority.register(EarthKind.UPDATE_KIND, new UpdateAuthority());
        EarthAuthority.register(EarthKind.OFFER_KIND, new OfferAuthority());
        EarthAuthority.register(EarthKind.PARTY_KIND, new PartyAuthority());
        EarthAuthority.register(EarthKind.MEMBER_KIND, new MemberAuthority());
        EarthAuthority.register(EarthKind.FORM_KIND, new FormAuthority());
        EarthAuthority.register(EarthKind.CLUB_KIND, new ClubAuthority());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(RequestMethod.GET, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(RequestMethod.POST, req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(RequestMethod.PUT, req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(RequestMethod.DELETE, req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addMainHeaders(resp);
    }

    private void addMainHeaders(final HttpServletResponse resp) {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.addHeader("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept");
    }

    private void handle(RequestMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addMainHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        try {
            EarthThing user = new Auth().fetchUserFromAuth(req.getParameter(Config.PARAM_EMAIL), req.getParameter(Config.PARAM_AUTH));

            Api.getService().call(user, method, req, resp);
        } catch (PrintingError e) {
            e.printStackTrace();
            errorOut(resp, e);
        } catch (StringResponse string) {
            try {
                resp.getWriter().write(string.getString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    @Override
    public void destroy() {
        ImageQueue.getService().stop();
        Queue.getService().stop();
        super.destroy();
    }
}