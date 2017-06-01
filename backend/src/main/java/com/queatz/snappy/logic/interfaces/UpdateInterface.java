package com.queatz.snappy.logic.interfaces;

import com.google.gson.JsonArray;
import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthRef;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.editors.LikeEditor;
import com.queatz.snappy.logic.editors.MemberEditor;
import com.queatz.snappy.logic.editors.UpdateEditor;
import com.queatz.snappy.logic.eventables.LikeEvent;
import com.queatz.snappy.logic.eventables.NewCommentEvent;
import com.queatz.snappy.logic.eventables.NewUpdateEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.LikeMine;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.logic.views.LikeView;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jacob on 5/9/16.
 */
public class UpdateInterface extends CommonThingInterface {

    @Override
    public String getThing(EarthAs as, EarthThing thing) {
        switch (as.getRoute().size()) {
            case 2:
                String updateId = as.getRoute().get(0);

                switch (as.getRoute().get(1)) {
                    case Config.PATH_LIKERS:
                        return getLikers(as, updateId);
                }
        }

        return null;
    }

    @Override
    public String postThing(EarthAs as, EarthThing thing) {
        switch (as.getRoute().size()) {
            case 2:
                if (Config.PATH_LIKE.equals(as.getRoute().get(1))) {
                    return likeUpdate(as, as.getRoute().get(0));
                }
        }

        return null;
    }

    private String likeUpdate(EarthAs as, String updateId) {
        as.requireUser();

        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        EarthThing poster = new EarthStore(as).get(updateId);

        EarthThing like = new LikeMine(as).getLike(as.getUser(), poster);

        if (like != null) {
            return new SuccessView(false).toJson();
        }

        like = new LikeEditor(as).newLike(as.getUser(), poster);

        new EarthUpdate(as).send(new LikeEvent(like))
                .to(poster.getKey(EarthField.SOURCE));

        return new LikeView(as, like).setLocalId(localId).toJson();
    }

    private String getLikers(EarthAs as, String updateId) {
        List<EarthThing> likers = new EarthStore(as).find(EarthKind.LIKE_KIND, EarthField.TARGET, EarthRef.of(updateId));

        return new EntityListView(as, likers, EarthView.SHALLOW).toJson();
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing update) {
        String message = null;
        boolean photoUploaded = update.getBoolean(EarthField.PHOTO);

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(as.getRequest());
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    ApiUtil.putPhoto(update.key().name(), as.getApi(),  as.getRequest());
                    photoUploaded = true;
                }
                else if (Config.PARAM_MESSAGE.equals(item.getFieldName())) {
                    message = Streams.asString(stream, "UTF-8");
                }
            }
        }
        catch (FileUploadException | IOException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("upto photo - couldn't upload because: " + e);
        }

        if (message == null && !photoUploaded) {
            throw new NothingLogicResponse("post update - nothing to post");
        }

        update = new UpdateEditor(as).updateWith(update, message, photoUploaded);

        return update;
    }

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        EarthThing update = new UpdateEditor(as).stageUpdate(as.getUser());

        String message = null;
        boolean photoUploaded = false;
        String thingId = null;

        Double latitude = null;
        Double longitude = null;

        JsonArray with = null;
        boolean going = false;

        // XXX TODO Make this use ApiUtil.putPhoto (with support for reading other params)

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(as.getRequest());
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    ApiUtil.putPhoto(update.key().name(), as.getApi(), item);
                    photoUploaded = true;
                }
                else if (Config.PARAM_MESSAGE.equals(item.getFieldName())) {
                    message = Streams.asString(stream, "UTF-8");
                }
                else if (Config.PARAM_LATITUDE.equals(item.getFieldName())) {
                    latitude = Double.parseDouble(Streams.asString(stream, "UTF-8"));
                }
                else if (Config.PARAM_LONGITUDE.equals(item.getFieldName())) {
                    longitude = Double.parseDouble(Streams.asString(stream, "UTF-8"));
                }
                else if (Config.PARAM_WITH.equals(item.getFieldName())) {
                    with = new EarthJson().fromJson(Streams.asString(stream, "UTF-8"), JsonArray.class);
                }
                else if (Config.PARAM_GOING.equals(item.getFieldName())) {
                    going = Boolean.parseBoolean(Streams.asString(stream, "UTF-8"));
                }
            }
        }
        catch (FileUploadException | IOException e) {
            new EarthStore(as).conclude(update);
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("post photo - couldn't upload because: " + e);
        }

        if (thingId == null) {
            new EarthStore(as).conclude(update);
            throw new NothingLogicResponse("post update - no thing");
        }

        if (message == null && !photoUploaded && with == null) {
            new EarthStore(as).conclude(update);
            throw new NothingLogicResponse("post update - nothing to post");
        }

        EarthThing thing = new EarthStore(as).get(thingId);

        EarthGeo geo = null;

        if (latitude != null && longitude != null) {
            geo = EarthGeo.of(latitude, longitude);
        }

        update = new UpdateEditor(as).updateWith(update, thing, message, photoUploaded, geo, with, going);

        if (EarthKind.UPDATE_KIND.equals(thing.getString(EarthField.KIND))) {
            new EarthUpdate(as).send(new NewCommentEvent(update)).to(thing.getKey(EarthField.SOURCE));
        } else {
            new EarthUpdate(as).send(new NewUpdateEvent(update)).toFollowersOf(thing);
        }

        return update;
    }
}
