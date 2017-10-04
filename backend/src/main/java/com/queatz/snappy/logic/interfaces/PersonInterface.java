package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthRef;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.editors.FollowerEditor;
import com.queatz.snappy.logic.editors.MessageEditor;
import com.queatz.snappy.logic.editors.RecentEditor;
import com.queatz.snappy.logic.eventables.FollowEvent;
import com.queatz.snappy.logic.eventables.InformationEvent;
import com.queatz.snappy.logic.eventables.MessageEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.FollowerMine;
import com.queatz.snappy.logic.mines.MessageMine;
import com.queatz.snappy.logic.mines.RecentMine;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jacob on 5/9/16.
 */
public class PersonInterface extends CommonThingInterface {

    @Override
    public String getThing(EarthAs as, EarthThing earthThing) {
        switch (as.getRoute().size()) {
            case 2:
                String personId = as.getRoute().get(0);

                switch (as.getRoute().get(1)) {
                    case Config.PATH_FOLLOWERS:
                        return getFollows(as, true, personId);
                    case Config.PATH_FOLLOWING:
                        return getFollows(as, false, personId);
                    case Config.PATH_PARTIES:
                        return getParties(as, personId);
                    case Config.PATH_MESSAGES:
                        return getMessages(as, personId);
                }
        }

        throw new NothingLogicResponse("people - bad path");
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing thing) {
            EarthThing person = new EarthStore(as).get(as.getRoute().get(0));

            if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_SEEN))) {
               postSeen(as, person);
            } else if (Boolean.toString(true).equals(as.getRequest().getParameter(Config.PARAM_FOLLOW))) {
                person = postFollow(as, person);
            } else if (Boolean.toString(false).equals(as.getRequest().getParameter(Config.PARAM_FOLLOW))) {
                postStopFollowing(as, person);
            } else {
                String message = as.getRequest().getParameter(Config.PARAM_MESSAGE);

                if (message != null) {
                    person = postMessage(as, person, message);
                }
        }

        return person;
    }

    @Override
    public void onGet(EarthAs as, EarthThing person) {
        // Update location of other person if auth'd
        if (person != null && as.hasUser()) {
            if (!as.getUser().key().equals(person.key())) {
                new EarthUpdate(as).send(new InformationEvent()).to(person);
            }
        }
    }

    @Override
    public String postThing(EarthAs as, EarthThing thing) {
        switch (as.getRoute().size()) {
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_MESSAGE:
                        return new EarthViewer(as).getViewForEntityOrThrow(
                                postMessage(as, new EarthStore(as).get(as.getRoute().get(0)))
                        ).toJson();
                }
                break;
        }

        return null;
    }

    @Override
    public EarthThing createThing(EarthAs as) {
        throw new NothingLogicResponse("not this way");
    }

    private String getFollows(EarthAs as, boolean followers, String personId) {
        EarthThing person = new EarthStore(as).get(personId);

        List<EarthThing> follows = new EarthStore(as).find(EarthKind.FOLLOWER_KIND,
                followers ? EarthField.TARGET : EarthField.SOURCE,
                person.key());

        return new EntityListView(as, follows, EarthView.SHALLOW).toJson();
    }

    private String getParties(EarthAs as, String personId) {
        EarthThing person = new EarthStore(as).get(personId);

        List<EarthThing> follows = new EarthStore(as).find(EarthKind.FOLLOWER_KIND,
                EarthField.HOST,
                person.key());

        return new EntityListView(as, follows, EarthView.SHALLOW).toJson();
    }

    private String getMessages(EarthAs as, String personId) {
        as.requireUser();

        // XXX TODO when Datastore supports OR expressions, combine these
        List<EarthThing> messagesToMe = new MessageMine(as).messagesFromTo(as.getUser().key(), EarthRef.of(personId));
        List<EarthThing> messagesFromMe = new MessageMine(as).messagesFromTo(EarthRef.of(personId), as.getUser().key());

        List<EarthThing> messages = new ArrayList<>();
        messages.addAll(messagesToMe);
        messages.addAll(messagesFromMe);

        return new EntityListView(as, messages).toJson();
    }

    private String postSeen(EarthAs as, EarthThing personId) {
        as.requireUser();

        EarthThing recent = new RecentMine(as).byPerson(as.getUser(), personId);

        if (recent == null) {
            throw new NothingLogicResponse("people - recent - not found");
        }

        new RecentEditor(as).markSeen(recent);
        return new SuccessView(true).toJson();
    }

    private EarthThing postFollow(EarthAs as, EarthThing person) {
        as.requireUser();

        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        EarthThing follow;

        follow = new FollowerMine(as).getFollower(as.getUser(), person);

        if (follow == null) {
            follow = new FollowerEditor(as).newFollower(as.getUser(), person);

            new EarthUpdate(as).send(new FollowEvent(follow))
                    .to(follow.getKey(EarthField.TARGET));
        }

        return follow.setLocalId(localId);
    }

    private String postStopFollowing(EarthAs as, EarthThing person) {
        as.requireUser();

        EarthThing follower = new FollowerMine(as).getFollower(as.getUser(), person);
        new EarthStore(as).conclude(follower);

        return new SuccessView(true).toJson();
    }

    private EarthThing postMessage(EarthAs as, EarthThing person, String message) {
        as.requireUser();

        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);
        EarthThing sent = new MessageEditor(as).newMessage(as.getUser(), person, message, false);

        new RecentEditor(as).updateWithMessage(sent);

        new EarthUpdate(as).send(new MessageEvent(sent)).to(person);

        return sent.setLocalId(localId);
    }

    private EarthThing postMessage(EarthAs as, EarthThing person)  {
        as.requireUser();

        EarthThing sent = new MessageEditor(as).stageMessage(as.getUser(), person);
        String photoName = "earth/thing/photo/" + sent.key().name();

        String message = null;
        String localId = null;
        boolean photoUploaded = false;

        // XXX TODO Make this use ApiUtil.putPhoto (with support for reading other params)
        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(as.getRequest());
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    int len;
                    byte[] buffer = new byte[8192];

                    OutputStream outputChannel = as.getApi().snappyImage.openOutputStream(photoName, item.getName());

                    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                        outputChannel.write(buffer, 0, len);
                    }

                    outputChannel.close();

                    photoUploaded = true;
                }
                else if (Config.PARAM_MESSAGE.equals(item.getFieldName())) {
                    message = Streams.asString(stream, "UTF-8");
                }
                else if (Config.PARAM_LOCAL_ID.equals(item.getFieldName())) {
                    localId = Streams.asString(stream, "UTF-8");
                }
            }
        }
        catch (FileUploadException | IOException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("message photo - couldn't upload because: " + e);
        }

        if (message == null && !photoUploaded) {
            throw new NothingLogicResponse("message - nothing to post");
        }

        sent = new MessageEditor(as).setMessage(sent, message, photoUploaded);

        new RecentEditor(as).updateWithMessage(sent);

        new EarthUpdate(as).send(new MessageEvent(sent)).to(person);

        return sent.setLocalId(localId);
    }

}
