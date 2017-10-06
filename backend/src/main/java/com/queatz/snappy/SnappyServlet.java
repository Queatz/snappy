package com.queatz.snappy;

import com.queatz.earth.EarthAuthority;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.api.Admin;
import com.queatz.snappy.api.Api;
import com.queatz.snappy.api.Logic;
import com.queatz.snappy.api.Pirate;
import com.queatz.snappy.api.RequestMethod;
import com.queatz.snappy.api.StringResponse;
import com.queatz.snappy.appstore.AppInterface;
import com.queatz.snappy.events.Queue;
import com.queatz.snappy.exceptions.PrintingError;
import com.queatz.snappy.images.ImageQueue;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.router.EarthRouter;
import com.queatz.snappy.logic.interfaces.ByNameInterface;
import com.queatz.snappy.logic.interfaces.ClubInterface;
import com.queatz.snappy.logic.interfaces.ContactInterface;
import com.queatz.snappy.logic.interfaces.FeedbackInterface;
import com.queatz.snappy.logic.interfaces.FollowerInterface;
import com.queatz.snappy.logic.interfaces.FormInterface;
import com.queatz.snappy.logic.interfaces.FormItemInterface;
import com.queatz.snappy.logic.interfaces.FormSubmissionInterface;
import com.queatz.snappy.logic.interfaces.GeoSubscribeInterface;
import com.queatz.snappy.logic.interfaces.HereInterface;
import com.queatz.snappy.logic.interfaces.HubInterface;
import com.queatz.snappy.logic.interfaces.JoinInterface;
import com.queatz.snappy.logic.interfaces.LikeInterface;
import com.queatz.snappy.logic.interfaces.LocationInterface;
import com.queatz.snappy.logic.interfaces.MeInterface;
import com.queatz.snappy.logic.interfaces.MemberInterface;
import com.queatz.snappy.logic.interfaces.MessageInterface;
import com.queatz.snappy.logic.interfaces.OfferInterface;
import com.queatz.snappy.logic.interfaces.PartyInterface;
import com.queatz.snappy.logic.interfaces.PersonInterface;
import com.queatz.snappy.logic.interfaces.ProjectInterface;
import com.queatz.snappy.logic.interfaces.RecentInterface;
import com.queatz.snappy.logic.interfaces.ResourceInterface;
import com.queatz.snappy.logic.interfaces.SearchInterface;
import com.queatz.snappy.logic.interfaces.UpdateInterface;
import com.queatz.snappy.shared.EarthSpecialRoute;
import com.queatz.snappy.view.EarthViewer;
import com.queatz.snappy.logic.authorities.ClubAuthority;
import com.queatz.snappy.logic.authorities.FormAuthority;
import com.queatz.snappy.logic.authorities.HubAuthority;
import com.queatz.snappy.logic.authorities.MemberAuthority;
import com.queatz.snappy.logic.authorities.MessageAuthority;
import com.queatz.snappy.logic.authorities.OfferAuthority;
import com.queatz.snappy.logic.authorities.PartyAuthority;
import com.queatz.snappy.logic.views.ClubView;
import com.queatz.snappy.logic.views.ContactView;
import com.queatz.snappy.logic.views.FollowerView;
import com.queatz.snappy.logic.views.FormSubmissionView;
import com.queatz.snappy.logic.views.FormView;
import com.queatz.snappy.logic.views.HubView;
import com.queatz.snappy.logic.views.JoinView;
import com.queatz.snappy.logic.views.LikeView;
import com.queatz.snappy.logic.views.LocationView;
import com.queatz.snappy.logic.views.MemberView;
import com.queatz.snappy.logic.views.MessageView;
import com.queatz.snappy.logic.views.OfferView;
import com.queatz.snappy.logic.views.PartyView;
import com.queatz.snappy.logic.views.PersonView;
import com.queatz.snappy.logic.views.ProjectView;
import com.queatz.snappy.logic.views.RecentView;
import com.queatz.snappy.logic.views.ResourceView;
import com.queatz.snappy.logic.views.UpdateView;
import com.vlllage.things.PersonAuthority;
import com.queatz.snappy.logic.authorities.ProjectAuthority;
import com.queatz.snappy.logic.authorities.ResourceAuthority;
import com.queatz.snappy.logic.authorities.UpdateAuthority;
import com.queatz.snappy.logic.eventables.ClearNotificationEvent;
import com.queatz.snappy.logic.eventables.FollowEvent;
import com.queatz.snappy.logic.eventables.FormSubmissionEvent;
import com.queatz.snappy.logic.eventables.InformationEvent;
import com.queatz.snappy.logic.eventables.JoinAcceptedEvent;
import com.queatz.snappy.logic.eventables.JoinRequestEvent;
import com.queatz.snappy.logic.eventables.LikeEvent;
import com.queatz.snappy.logic.eventables.MessageEvent;
import com.queatz.snappy.logic.eventables.NewCommentEvent;
import com.queatz.snappy.logic.eventables.NewContactEvent;
import com.queatz.snappy.logic.eventables.NewOfferEvent;
import com.queatz.snappy.logic.eventables.NewPartyEvent;
import com.queatz.snappy.logic.eventables.NewThingEvent;
import com.queatz.snappy.logic.eventables.NewUpdateEvent;
import com.queatz.snappy.logic.eventables.OfferLikeEvent;
import com.queatz.snappy.logic.eventables.RefreshMeEvent;
import com.queatz.snappy.authenticate.Auth;
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

        EarthUpdate.register(Config.PUSH_ACTION_JOIN_ACCEPTED, JoinAcceptedEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_JOIN_REQUEST, JoinRequestEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_MESSAGE, MessageEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_NEW_PARTY, NewPartyEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_CLEAR_NOTIFICATION, ClearNotificationEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_NEW_UPTO, NewUpdateEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_NEW_OFFER, NewOfferEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_LIKE_UPDATE, LikeEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_OFFER_LIKED, OfferLikeEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_NEW_THING, NewThingEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_NEW_CONTACT, NewContactEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_FOLLOW, FollowEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_REFRESH_ME, RefreshMeEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_NEW_COMMENT, NewCommentEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_INFORMATION, InformationEvent.class);
        EarthUpdate.register(Config.PUSH_ACTION_FORM_SUBMISSION_EVENT, FormSubmissionEvent.class);

        EarthViewer.register(EarthKind.HUB_KIND, HubView.class);
        EarthViewer.register(EarthKind.CLUB_KIND, ClubView.class);
        EarthViewer.register(EarthKind.CONTACT_KIND, ContactView.class);
        EarthViewer.register(EarthKind.FOLLOWER_KIND, FollowerView.class);
        EarthViewer.register(EarthKind.LIKE_KIND, LikeView.class);
        EarthViewer.register(EarthKind.OFFER_KIND, OfferView.class);
        EarthViewer.register(EarthKind.MESSAGE_KIND, MessageView.class);
        EarthViewer.register(EarthKind.PERSON_KIND, PersonView.class);
        EarthViewer.register(EarthKind.PARTY_KIND, PartyView.class);
        EarthViewer.register(EarthKind.LOCATION_KIND, LocationView.class);
        EarthViewer.register(EarthKind.RECENT_KIND, RecentView.class);
        EarthViewer.register(EarthKind.UPDATE_KIND, UpdateView.class);
        EarthViewer.register(EarthKind.JOIN_KIND, JoinView.class);
        EarthViewer.register(EarthKind.RESOURCE_KIND, ResourceView.class);
        EarthViewer.register(EarthKind.PROJECT_KIND, ProjectView.class);
        EarthViewer.register(EarthKind.MEMBER_KIND, MemberView.class);
        EarthViewer.register(EarthKind.FORM_KIND, FormView.class);
        EarthViewer.register(EarthKind.FORM_SUBMISSION_KIND, FormSubmissionView.class);

        EarthRouter.register(EarthKind.HUB_KIND, new HubInterface());
        EarthRouter.register(EarthKind.CLUB_KIND, new ClubInterface());
        EarthRouter.register(EarthKind.CONTACT_KIND, new ContactInterface());
        EarthRouter.register(EarthKind.FOLLOWER_KIND, new FollowerInterface());
        EarthRouter.register(EarthKind.LIKE_KIND, new LikeInterface());
        EarthRouter.register(EarthKind.OFFER_KIND, new OfferInterface());
        EarthRouter.register(EarthKind.LOCATION_KIND, new LocationInterface());
        EarthRouter.register(EarthKind.MESSAGE_KIND, new MessageInterface());
        EarthRouter.register(EarthKind.PERSON_KIND, new PersonInterface());
        EarthRouter.register(EarthKind.RECENT_KIND, new RecentInterface());
        EarthRouter.register(EarthKind.UPDATE_KIND, new UpdateInterface());
        EarthRouter.register(EarthKind.JOIN_KIND, new JoinInterface());
        EarthRouter.register(EarthKind.RESOURCE_KIND, new ResourceInterface());
        EarthRouter.register(EarthKind.PROJECT_KIND, new ProjectInterface());
        EarthRouter.register(EarthKind.PARTY_KIND, new PartyInterface());
        EarthRouter.register(EarthKind.MEMBER_KIND, new MemberInterface());
        EarthRouter.register(EarthKind.FORM_KIND, new FormInterface());
        EarthRouter.register(EarthKind.FORM_ITEM_KIND, new FormItemInterface());
        EarthRouter.register(EarthKind.FORM_SUBMISSION_KIND, new FormSubmissionInterface());

        EarthRouter.registerSpecial(EarthSpecialRoute.HERE_ROUTE, new HereInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.ME_ROUTE, new MeInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.BY_NAME_ROUTE, new ByNameInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.FEEDBACK_ROUTE, new FeedbackInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.SEARCH_ROUTE, new SearchInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.GEO_SUBSCRIBE_ROUTE, new GeoSubscribeInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.APP_ROUTE, new AppInterface());
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