package com.village.things;

import com.google.gson.JsonArray;
import com.image.SnappyImage;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.api.ApiUtil;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.shared.earth.EarthRef;
import com.queatz.snappy.view.EarthView;
import com.queatz.snappy.view.SuccessView;

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

        EarthThing poster = as.s(EarthStore.class).get(updateId);

        EarthThing like = as.s(LikeMine.class).getLike(as.getUser(), poster);

        if (like != null) {
            return new SuccessView(false).toJson();
        }

        like = as.s(LikeEditor.class).newLike(as.getUser(), poster);

        as.s(EarthUpdate.class).send(new LikeEvent(like))
                .to(poster.getKey(EarthField.SOURCE));

        return new SuccessView(true).toJson();
    }

    private String getLikers(EarthAs as, String updateId) {
        List<EarthThing> likers = as.s(EarthStore.class).find(EarthKind.LIKE_KIND, EarthField.TARGET, EarthRef.of(updateId));

        return new EntityListView(as, likers, EarthView.SHALLOW).toJson();
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing update) {
        String message = null, hidden = null, clubs = null;
        boolean photoUploaded = update.getBoolean(EarthField.PHOTO);

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(as.getRequest());
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    ApiUtil.putPhoto(update.key().name(), as.s(SnappyImage.class), as.getRequest());
                    photoUploaded = true;
                }
                else if (Config.PARAM_MESSAGE.equals(item.getFieldName())) {
                    message = Streams.asString(stream, "UTF-8");
                } else if (Config.PARAM_HIDDEN.equals(item.getFieldName())) {
                    hidden = Streams.asString(stream, "UTF-8");
                } else if (Config.PARAM_CLUBS.equals(item.getFieldName())) {
                    clubs = Streams.asString(stream, "UTF-8");
                }

                stream.close();
            }
        }
        catch (FileUploadException | IOException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("upto photo - couldn't upload because: " + e);
        }

        if (message == null && !photoUploaded) {
            throw new NothingLogicResponse("post update - nothing to post");
        }

        update = as.s(UpdateEditor.class).updateWith(update, message, photoUploaded);

        if (hidden != null) {
            setVisibilityHidden(as, update, hidden);
        }

        if (clubs != null) {
            setVisibilityClubs(as, update, clubs);
        }

        return update;
    }

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        EarthThing update = as.s(UpdateEditor.class).stageUpdate(as.getUser());

        String message = null, hidden = null, clubs = null;
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
                    ApiUtil.putPhoto(update.key().name(), item.getName(), as.s(SnappyImage.class), item);
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
                    with = as.s(EarthJson.class).fromJson(Streams.asString(stream, "UTF-8"), JsonArray.class);
                }
                else if (Config.PARAM_GOING.equals(item.getFieldName())) {
                    going = Boolean.parseBoolean(Streams.asString(stream, "UTF-8"));
                }
                else if (Config.PARAM_IN.equals(item.getFieldName())) {
                    thingId = Streams.asString(stream, "UTF-8");
                } else if (Config.PARAM_HIDDEN.equals(item.getFieldName())) {
                    hidden = Streams.asString(stream, "UTF-8");
                } else if (Config.PARAM_CLUBS.equals(item.getFieldName())) {
                    clubs = Streams.asString(stream, "UTF-8");
                }

                stream.close();
            }
        }
        catch (FileUploadException | IOException e) {
            as.s(EarthStore.class).conclude(update);
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("post photo - couldn't upload because: " + e);
        }

        if (thingId == null) {
            as.s(EarthStore.class).conclude(update);
            throw new NothingLogicResponse("post update - no thing");
        }

        if (message == null && !photoUploaded && with == null) {
            as.s(EarthStore.class).conclude(update);
            throw new NothingLogicResponse("post update - nothing to post");
        }

        EarthThing thing = as.s(EarthStore.class).get(thingId);

        EarthGeo geo = null;

        if (latitude != null && longitude != null) {
            geo = EarthGeo.of(latitude, longitude);
        }

        update = as.s(UpdateEditor.class).updateWith(update, thing, message, photoUploaded, geo, with, going);

        isIn(as, update, thing);

        if (EarthKind.UPDATE_KIND.equals(thing.getString(EarthField.KIND))) {
            as.s(EarthUpdate.class).send(new NewCommentEvent(update)).to(thing.getKey(EarthField.SOURCE));
        } else {
            as.s(EarthUpdate.class).send(new NewUpdateEvent(update)).toFollowersOf(thing);
        }

        if (hidden != null) {
            setVisibilityHidden(as, update, hidden);
        }

        if (clubs != null) {
            setVisibilityClubs(as, update, clubs);
        }

        return update;
    }
}
