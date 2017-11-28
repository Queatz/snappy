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
import com.queatz.snappy.authenticate.Auth;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.events.Queue;
import com.queatz.snappy.exceptions.PrintingError;
import com.queatz.snappy.images.ImageQueue;
import com.queatz.snappy.logic.interfaces.ByNameInterface;
import com.queatz.snappy.logic.interfaces.FeedbackInterface;
import com.queatz.snappy.logic.interfaces.HereInterface;
import com.queatz.snappy.logic.interfaces.MeInterface;
import com.queatz.snappy.logic.interfaces.MemberInterface;
import com.queatz.snappy.logic.interfaces.SearchInterface;
import com.queatz.snappy.plugins.ContactEditorPlugin;
import com.queatz.snappy.plugins.ContactMinePlugin;
import com.queatz.snappy.plugins.EarthPlugin;
import com.queatz.snappy.plugins.FollowerMinePlugin;
import com.queatz.snappy.plugins.MemberEditorPlugin;
import com.queatz.snappy.plugins.MemberMinePlugin;
import com.queatz.snappy.router.EarthRouter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthSpecialRoute;
import com.queatz.snappy.view.EarthViewer;
import com.village.things.ActionAuthority;
import com.village.things.ActionChangeEvent;
import com.village.things.ActionInterface;
import com.village.things.ActionQueue;
import com.village.things.ActionView;
import com.village.things.ClearNotificationEvent;
import com.village.things.ClubAuthority;
import com.village.things.ClubInterface;
import com.village.things.ClubView;
import com.village.things.ContactAuthority;
import com.village.things.ContactEditor;
import com.village.things.ContactInterface;
import com.village.things.ContactMine;
import com.village.things.ContactView;
import com.village.things.FollowEvent;
import com.village.things.FollowerInterface;
import com.village.things.FollowerMine;
import com.village.things.FollowerView;
import com.village.things.FormAuthority;
import com.village.things.FormInterface;
import com.village.things.FormItemInterface;
import com.village.things.FormSubmissionEvent;
import com.village.things.FormSubmissionInterface;
import com.village.things.FormSubmissionView;
import com.village.things.FormView;
import com.village.things.GeoSubscribeInterface;
import com.village.things.HubAuthority;
import com.village.things.HubInterface;
import com.village.things.HubView;
import com.village.things.InformationEvent;
import com.village.things.JoinAcceptedEvent;
import com.village.things.JoinInterface;
import com.village.things.JoinRequestEvent;
import com.village.things.JoinView;
import com.village.things.LikeEvent;
import com.village.things.LikeInterface;
import com.village.things.LikeView;
import com.village.things.LocationInterface;
import com.village.things.LocationView;
import com.village.things.MemberAuthority;
import com.village.things.MemberEditor;
import com.village.things.MemberMine;
import com.village.things.MemberView;
import com.village.things.MessageAuthority;
import com.village.things.MessageEvent;
import com.village.things.MessageInterface;
import com.village.things.MessageView;
import com.village.things.ModeAuthority;
import com.village.things.ModeInterface;
import com.village.things.ModeView;
import com.village.things.NewCommentEvent;
import com.village.things.NewContactEvent;
import com.village.things.NewOfferEvent;
import com.village.things.NewPartyEvent;
import com.village.things.NewThingEvent;
import com.village.things.NewUpdateEvent;
import com.village.things.OfferAuthority;
import com.village.things.OfferInterface;
import com.village.things.OfferLikeEvent;
import com.village.things.OfferView;
import com.village.things.PartyAuthority;
import com.village.things.PartyInterface;
import com.village.things.PartyView;
import com.village.things.PersonAuthority;
import com.village.things.PersonInterface;
import com.village.things.PersonView;
import com.village.things.ProjectAuthority;
import com.village.things.ProjectInterface;
import com.village.things.ProjectView;
import com.village.things.RecentInterface;
import com.village.things.RecentView;
import com.village.things.RefreshMeEvent;
import com.village.things.ResourceAuthority;
import com.village.things.ResourceInterface;
import com.village.things.ResourceView;
import com.village.things.UpdateAuthority;
import com.village.things.UpdateInterface;
import com.village.things.UpdateView;
import com.vlllage.graph.EarthGraph;
import com.vlllage.graph.fields.AboutEarthGraphField;
import com.vlllage.graph.fields.ActionEarthGraphField;
import com.vlllage.graph.fields.AroundEarthGraphField;
import com.vlllage.graph.fields.AspectEarthGraphField;
import com.vlllage.graph.fields.AuthEarthGraphField;
import com.vlllage.graph.fields.BackingEarthGraphField;
import com.vlllage.graph.fields.ClubsEarthGraphField;
import com.vlllage.graph.fields.DataEarthGraphField;
import com.vlllage.graph.fields.DateEarthGraphField;
import com.vlllage.graph.fields.FirstNameEarthGraphField;
import com.vlllage.graph.fields.GeoEarthGraphField;
import com.vlllage.graph.fields.GoingEarthGraphField;
import com.vlllage.graph.fields.GoogleUrlEarthGraphField;
import com.vlllage.graph.fields.HiddenEarthGraphField;
import com.vlllage.graph.fields.IdEarthGraphField;
import com.vlllage.graph.fields.ImageUrlEarthGraphField;
import com.vlllage.graph.fields.InEarthGraphField;
import com.vlllage.graph.fields.InfoDistanceEarthGraphField;
import com.vlllage.graph.fields.JoinsEarthGraphField;
import com.vlllage.graph.fields.KindEarthGraphField;
import com.vlllage.graph.fields.LastNameEarthGraphField;
import com.vlllage.graph.fields.LatestEarthGraphField;
import com.vlllage.graph.fields.MembersEarthGraphField;
import com.vlllage.graph.fields.MessageEarthGraphField;
import com.vlllage.graph.fields.NameEarthGraphField;
import com.vlllage.graph.fields.OwnerEarthGraphField;
import com.vlllage.graph.fields.PhotoEarthGraphField;
import com.vlllage.graph.fields.RoleEarthGraphField;
import com.vlllage.graph.fields.SeenEarthGraphField;
import com.vlllage.graph.fields.SourceEarthGraphField;
import com.vlllage.graph.fields.TargetEarthGraphField;
import com.vlllage.graph.fields.TokenEarthGraphField;
import com.vlllage.graph.fields.TypeEarthGraphField;
import com.vlllage.graph.fields.UpdatedEarthGraphField;

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
        EarthAuthority.register(EarthKind.CONTACT_KIND, new ContactAuthority());
        EarthAuthority.register(EarthKind.CLUB_KIND, new ClubAuthority());
        EarthAuthority.register(EarthKind.MODE_KIND, new ModeAuthority());
        EarthAuthority.register(EarthKind.ACTION_KIND, new ActionAuthority());

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
        EarthUpdate.register(Config.PUSH_ACTION_ACTION_CHANGE, ActionChangeEvent.class);

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
        EarthViewer.register(EarthKind.MODE_KIND, ModeView.class);
        EarthViewer.register(EarthKind.ACTION_KIND, ActionView.class);

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
        EarthRouter.register(EarthKind.MODE_KIND, new ModeInterface());
        EarthRouter.register(EarthKind.ACTION_KIND, new ActionInterface());

        EarthRouter.registerSpecial(EarthSpecialRoute.HERE_ROUTE, new HereInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.ME_ROUTE, new MeInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.BY_NAME_ROUTE, new ByNameInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.FEEDBACK_ROUTE, new FeedbackInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.SEARCH_ROUTE, new SearchInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.GEO_SUBSCRIBE_ROUTE, new GeoSubscribeInterface());
        EarthRouter.registerSpecial(EarthSpecialRoute.APP_ROUTE, new AppInterface());

        EarthGraph.register("id", new IdEarthGraphField());
        EarthGraph.register("kind", new KindEarthGraphField());
        EarthGraph.register("name", new NameEarthGraphField());
        EarthGraph.register("firstName", new FirstNameEarthGraphField());
        EarthGraph.register("lastName", new LastNameEarthGraphField());
        EarthGraph.register("about", new AboutEarthGraphField());
        EarthGraph.register("members", new MembersEarthGraphField());
        EarthGraph.register("in", new InEarthGraphField());
        EarthGraph.register("joins", new JoinsEarthGraphField());
        EarthGraph.register("source", new SourceEarthGraphField());
        EarthGraph.register("target", new TargetEarthGraphField());
        EarthGraph.register("owner", new OwnerEarthGraphField());
        EarthGraph.register("photo", new PhotoEarthGraphField());
        EarthGraph.register("aspect", new AspectEarthGraphField());
        EarthGraph.register("backing", new BackingEarthGraphField());
//        EarthGraph.register("backers", new BackersEarthGraphField());
        EarthGraph.register("hidden", new HiddenEarthGraphField());
        EarthGraph.register("clubs", new ClubsEarthGraphField());

        EarthGraph.register("googleUrl", new GoogleUrlEarthGraphField());
        EarthGraph.register("imageUrl", new ImageUrlEarthGraphField());
        EarthGraph.register("infoDistance", new InfoDistanceEarthGraphField());
        EarthGraph.register("auth", new AuthEarthGraphField());
        EarthGraph.register("going", new GoingEarthGraphField());
        EarthGraph.register("geo", new GeoEarthGraphField());
        EarthGraph.register("date", new DateEarthGraphField());
        EarthGraph.register("action", new ActionEarthGraphField());
        EarthGraph.register("message", new MessageEarthGraphField());
        EarthGraph.register("latest", new LatestEarthGraphField());
        EarthGraph.register("seen", new SeenEarthGraphField());
        EarthGraph.register("updated", new UpdatedEarthGraphField());

        EarthGraph.register("token", new TokenEarthGraphField());
        EarthGraph.register("data", new DataEarthGraphField());
        EarthGraph.register("type", new TypeEarthGraphField());
        EarthGraph.register("role", new RoleEarthGraphField());
        EarthGraph.register("around", new AroundEarthGraphField());

        EarthPlugin.register(MemberEditorPlugin.class, MemberEditor.class);
        EarthPlugin.register(MemberMinePlugin.class, MemberMine.class);
        EarthPlugin.register(ContactMinePlugin.class, ContactMine.class);
        EarthPlugin.register(ContactEditorPlugin.class, ContactEditor.class);
        EarthPlugin.register(FollowerMinePlugin.class, FollowerMine.class);
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
        } catch (StringResponse string) {
            try {
                resp.getWriter().write(string.getString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    @Override
    public void destroy() {
        ImageQueue.getService().stop();
        Queue.getService().stop();
        ActionQueue.getService().stop();
        super.destroy();
    }
}