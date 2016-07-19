package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.Lists;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.FollowerEditor;
import com.queatz.snappy.logic.editors.MessageEditor;
import com.queatz.snappy.logic.editors.RecentEditor;
import com.queatz.snappy.logic.eventables.FollowEvent;
import com.queatz.snappy.logic.eventables.MessageEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.FollowerMine;
import com.queatz.snappy.logic.mines.MessageMine;
import com.queatz.snappy.logic.mines.RecentMine;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.logic.views.FollowerView;
import com.queatz.snappy.logic.views.MessageView;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

import java.util.List;

/**
 * Created by jacob on 5/9/16.
 */
public class PersonInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                return getPerson(as, as.getRoute().get(0));
            case 2:
                String personId = as.getRoute().get(0);

                boolean followers = false;

                switch (as.getRoute().get(1)) {
                    case Config.PATH_FOLLOWERS:
                        followers = true;
                        // Fall-through
                    case Config.PATH_FOLLOWING:
                        return getFollows(as, followers, personId);
                    case Config.PATH_PARTIES:
                        return getParties(as, personId);
                    case Config.PATH_MESSAGES:
                        return getMessages(as, personId);
                }
        }

        throw new NothingLogicResponse("people - bad path");
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                Entity person = new EarthStore(as).get(as.getRoute().get(0));

                if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_SEEN))) {
                   return postSeen(as, person);
                } else if (Boolean.toString(true).equals(as.getRequest().getParameter(Config.PARAM_FOLLOW))) {
                    return postFollow(as, person);
                } else if (Boolean.toString(false).equals(as.getRequest().getParameter(Config.PARAM_FOLLOW))) {
                    return postStopFollowing(as, person);
                } else {
                    String message = as.getRequest().getParameter(Config.PARAM_MESSAGE);

                    if (message != null) {
                        return postMessage(as, person, message);
                    }
                }
        }

        throw new NothingLogicResponse("people - bad path");
    }

    private String getPerson(EarthAs as, String personId) {
        return new EarthViewer(as).getViewForEntityOrThrow(new EarthStore(as).get(personId)).toJson();
    }

    private String getFollows(EarthAs as, boolean followers, String personId) {
        Entity person = new EarthStore(as).get(personId);

        List<Entity> follows = new EarthStore(as).find(EarthKind.FOLLOWER_KIND,
                followers ? EarthField.TARGET : EarthField.SOURCE,
                person.key());

        return new EntityListView(as, follows).toJson();
    }

    private String getParties(EarthAs as, String personId) {
        Entity person = new EarthStore(as).get(personId);

        List<Entity> follows = new EarthStore(as).find(EarthKind.FOLLOWER_KIND,
                EarthField.HOST,
                person.key());

        return new EntityListView(as, follows).toJson();
    }

    private String getMessages(EarthAs as, String personId) {
        // XXX TODO when Datastore supports OR expressions, combine these
        List<Entity> messagesToMe = new MessageMine(as).messagesFromTo(as.getUser().key(), new EarthStore(as).key(personId));
        List<Entity> messagesFromMe = new MessageMine(as).messagesFromTo(new EarthStore(as).key(personId), as.getUser().key());

        List<Entity> messages = Lists.newArrayList();
        messages.addAll(messagesToMe);
        messages.addAll(messagesFromMe);

        return new EntityListView(as, messages).toJson();
    }

    private String postSeen(EarthAs as, Entity personId) {
        new RecentEditor(as).markSeen(new RecentMine(as).byPerson(as.getUser(), personId));
        return new SuccessView(true).toJson();
    }

    private String postFollow(EarthAs as, Entity person) {
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        Entity follow = new FollowerEditor(as).newFollower(as.getUser(), person);

        new EarthUpdate(as).send(new FollowEvent(follow))
                .to(follow.getKey(EarthField.TARGET));

        return new FollowerView(as, follow).setLocalId(localId).toJson();
    }

    private String postStopFollowing(EarthAs as, Entity person) {
        Entity follower = new FollowerMine(as).forPerson(as.getUser(), person);
        new EarthStore(as).conclude(follower);

        return new SuccessView(true).toJson();
    }

    private String postMessage(EarthAs as, Entity person, String message) {
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);
        Entity sent = new MessageEditor(as).newMessage(as.getUser(), person, message);

        new RecentEditor(as).updateWithMessage(sent);

        new EarthUpdate(as).send(new MessageEvent(sent)).to(person);

        return new MessageView(as, sent).setLocalId(localId).toJson();
    }
}
