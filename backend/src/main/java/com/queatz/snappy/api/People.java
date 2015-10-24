package com.queatz.snappy.api;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.Json;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.FollowLinkSpec;
import com.queatz.snappy.shared.things.MessageSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.io.IOException;

/**
 * Created by jacob on 2/14/15.
 */
public class People extends Api.Path {
    public People(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        String personId;

        switch (method) {
            case GET:
                if (path.size() == 1) {
                    getPerson(path.get(0));
                } else if (path.size() == 2) {
                    personId = path.get(0);

                    boolean followers = false;

                    switch (path.get(1)) {
                        case Config.PATH_FOLLOWERS:
                            followers = true;
                        case Config.PATH_FOLLOWING:
                            getFollows(followers, personId);
                            break;
                        case Config.PATH_PARTIES:
                            getParties(personId);
                            break;
                        default:
                            die("people - bad path");
                    }
                }
                else {
                    die("people - bad path");
                }

                break;
            case POST:
                if (path.size() != 1) {
                    die("people - bad path");
                }

                personId = path.get(0);

                if (Boolean.valueOf(request.getParameter(Config.PARAM_SEEN))) {
                    postSeen(personId);
                } else if (Boolean.toString(true).equals(request.getParameter(Config.PARAM_FOLLOW))) {
                    postFollow(personId);
                } else if (Boolean.toString(false).equals(request.getParameter(Config.PARAM_FOLLOW))) {
                    postStopFollowing(personId);
                } else {
                    String message = request.getParameter(Config.PARAM_MESSAGE);

                    if (message != null) {
                        postMessage(personId, message);
                    } else {
                        die("people - bad path");
                    }
                }

                break;
            default:
                die("people - bad method");
        }
    }

    private void getPerson(String personId){
        ok(Datastore.get(PersonSpec.class, personId));
    }

    private void getFollows(boolean followers, String personId) {
        PersonSpec person = Datastore.get(PersonSpec.class, personId);

        if (person == null) {
            notFound();
        }

        ok(Datastore.get(FollowLinkSpec.class).filter(followers ? "targetId" : "sourceId", person).list());
    }

    private void getParties(String personId) {
        PersonSpec person = Datastore.get(PersonSpec.class, personId);

        if (person == null) {
            notFound();
        }

        ok(Datastore.get(PartySpec.class).filter("hostId", Datastore.key(PersonSpec.class, personId)).list(), Json.Compression.SHALLOW);
    }

    private void postSeen(String personId) {
        ok(Thing.getService().contact.markSeen(user, personId));
    }

    private void postFollow(String personId) {
        PersonSpec person = Datastore.get(PersonSpec.class, personId);

        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        if (person != null) {
            FollowLinkSpec follow = Thing.getService().follow.createOrUpdate(user, person);

            if (follow != null) {
                follow.localId = localId;

                Push.getService().send(Datastore.id(follow.targetId), new PushSpec<>(Config.PUSH_ACTION_FOLLOW, follow));

                ok(follow);
            }
        }
    }

    private void postStopFollowing(String personId) {
        PersonSpec person = Datastore.get(PersonSpec.class, personId);

        if (person != null) {
            FollowLinkSpec follow = Thing.getService().follow.get(user, person);

            if (follow != null) {
                Thing.getService().follow.stopFollowing(follow);
            }
        }
    }

    private void postMessage(String personId, String message) throws IOException {
        PersonSpec person = Datastore.get(PersonSpec.class, personId);

        String localId = request.getParameter(Config.PARAM_LOCAL_ID);
        MessageSpec sent = Thing.getService().message.newMessage(user.id, person.id, message);

        if (sent != null) {
            sent.localId = localId;

            Push.getService().send(Datastore.id(sent.toId), new PushSpec<>(Config.PUSH_ACTION_MESSAGE, sent));

            ok(sent);
        }
    }
}
