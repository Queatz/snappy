package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.StructuredQuery;
import com.google.common.collect.Lists;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.FollowerEditor;
import com.queatz.snappy.logic.editors.MessageEditor;
import com.queatz.snappy.logic.editors.RecentEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.FollowerMine;
import com.queatz.snappy.logic.mines.PersonMine;
import com.queatz.snappy.logic.mines.RecentMine;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.logic.views.FollowerView;
import com.queatz.snappy.logic.views.MessageView;
import com.queatz.snappy.logic.views.PersonView;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

import java.util.List;

/**
 * Created by jacob on 5/9/16.
 */
public class PersonInterface implements Interfaceable {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    RecentMine recentMine = EarthSingleton.of(RecentMine.class);
    RecentEditor recentEditor = EarthSingleton.of(RecentEditor.class);
    FollowerEditor followerEditor = EarthSingleton.of(FollowerEditor.class);
    FollowerMine followerMine = EarthSingleton.of(FollowerMine.class);
    MessageEditor messageEditor = EarthSingleton.of(MessageEditor.class);

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
                Entity person = earthStore.get(as.getRoute().get(0));

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
        return new PersonView(earthStore.get(personId)).toJson();
    }

    private String getFollows(EarthAs as, boolean followers, String personId) {
        Entity person = earthStore.get(personId);

        List<Entity> follows = earthStore.find(EarthKind.FOLLOWER_KIND,
                followers ? EarthField.TARGET : EarthField.SOURCE,
                person.key());

        return new EntityListView(follows).toJson();
    }

    private String getParties(EarthAs as, String personId) {
        Entity person = earthStore.get(personId);

        List<Entity> follows = earthStore.find(EarthKind.FOLLOWER_KIND,
                EarthField.HOST,
                person.key());

        return new EntityListView(follows).toJson();
    }

    private String getMessages(EarthAs as, String personId) {
        // XXX TODO when Datastore supports OR expressions, combine these
        List<Entity> messagesToMe = Lists.newArrayList(
                earthStore.queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, as.getUser().key()),
                        StructuredQuery.PropertyFilter.eq(EarthField.TARGET, earthStore.key(personId))
                )
        );

        List<Entity> messagesFromMe = Lists.newArrayList(
                earthStore.queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, earthStore.key(personId)),
                        StructuredQuery.PropertyFilter.eq(EarthField.TARGET, as.getUser().key())
                )
        );

        List<Entity> messages = Lists.newArrayList();
        messages.addAll(messagesToMe);
        messages.addAll(messagesFromMe);

        return new EntityListView(messages).toJson();
    }

    private String postSeen(EarthAs as, Entity personId) {
        recentEditor.markSeen(recentMine.byPerson(as.getUser(), personId));
        return new SuccessView(true).toJson();
    }

    private String postFollow(EarthAs as, Entity person) {
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        Entity follow = followerEditor.newFollower(as.getUser(), person);

        Push.getService().send(follow.getKey(EarthField.TARGET).name(), new PushSpec<>(Config.PUSH_ACTION_FOLLOW, follow));

        return new FollowerView(follow).setLocalId(localId).toJson();
    }

    private String postStopFollowing(EarthAs as, Entity person) {
        Entity follower = followerMine.forPerson(as.getUser(), person);
        earthStore.conclude(follower);

        return new SuccessView(true).toJson();
    }

    private String postMessage(EarthAs as, Entity person, String message) {
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);
        Entity sent = messageEditor.newMessage(as.getUser(), person, message);

        recentEditor.updateWithMessage(sent);

        Push.getService().send(person.key().name(), new PushSpec<>(Config.PUSH_ACTION_MESSAGE, sent));

        return new MessageView(sent).setLocalId(localId).toJson();
    }
}
