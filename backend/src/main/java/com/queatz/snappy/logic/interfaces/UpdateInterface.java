package com.queatz.snappy.logic.interfaces;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.google.gson.JsonArray;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.LikeEditor;
import com.queatz.snappy.logic.editors.UpdateEditor;
import com.queatz.snappy.logic.eventables.LikeEvent;
import com.queatz.snappy.logic.eventables.NewCommentEvent;
import com.queatz.snappy.logic.eventables.NewUpdateEvent;
import com.queatz.snappy.logic.exceptions.LogicException;
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
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jacob on 5/9/16.
 */
public class UpdateInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                String updateId = as.getRoute().get(0);

                switch (as.getRoute().get(1)) {
                    case Config.PATH_LIKERS:
                        return getLikers(as, updateId);
                    case Config.PATH_PHOTO:
                        return getPhoto(as, updateId);
                    default:
                }
        }

        throw new NothingLogicResponse("update - bad path");
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                return postUpdate(as);
            case 1:
                String updateId = as.getRoute().get(0);

                String edit = as.getRequest().getParameter(Config.PARAM_EDIT);

                if (Boolean.toString(true).equals(edit)) {
                    return editUpdate(as, updateId);
                }
                break;
            case 2:
                if (Config.PATH_LIKE.equals(as.getRoute().get(1))) {
                    return likeUpdate(as, as.getRoute().get(0));
                }
                else if (Config.PATH_DELETE.equals(as.getRoute().get(1))) {
                    new EarthStore(as).conclude(as.getRoute().get(0));

                    return new SuccessView(true).toJson();
                }
        }

        throw new NothingLogicResponse("update - bad path");
    }

    private String likeUpdate(EarthAs as, String updateId) {
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
        List<EarthThing> likers = new EarthStore(as).find(EarthKind.LIKE_KIND, EarthField.TARGET, new EarthStore(as).key(updateId));

        return new EntityListView(as, likers, EarthView.SHALLOW).toJson();
    }

    private String getPhoto(EarthAs as, String updateId) {
        int size;

        try {
            size = Integer.parseInt(as.getRequest().getParameter(Config.PARAM_SIZE));
        } catch (NumberFormatException e) {
            size = 200;
        }

        // XXX TODO Make this use ApiUtil.getPhoto
        try {
            ListOptions options = new ListOptions.Builder().setPrefix("earth/thing/photo/" + updateId + "/").setRecursive(false).build();
            ListResult list = as.getApi().mGCS.list(as.getApi().mAppIdentityService.getDefaultGcsBucketName(), options);

            Date lastModified = new Date(0);
            String fileName = null;
            while (list.hasNext()) {
                ListItem item = list.next();
                if (!item.isDirectory() && lastModified.before(item.getLastModified())) {
                    lastModified = item.getLastModified();
                    fileName = item.getName();
                }
            }

            if (fileName == null) {
                throw new NothingLogicResponse("update photo - not found");
            }

            ImagesService imagesService = ImagesServiceFactory.getImagesService();
            ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withGoogleStorageFileName(
                    "/gs/" + as.getApi().mAppIdentityService.getDefaultGcsBucketName() + "/" + fileName).imageSize(size).secureUrl(true);
            String photoUrl = imagesService.getServingUrl(servingUrlOptions);

            as.getResponse().sendRedirect(photoUrl);
        } catch (IOException e) {
            throw new LogicException("update photo - io error");
        }

        return null;
    }

    private String postUpdate(EarthAs as) {
        EarthThing update = new UpdateEditor(as).stageUpdate(as.getUser());
        GcsFilename photoName = new GcsFilename(as.getApi().mAppIdentityService.getDefaultGcsBucketName(), "earth/thing/photo/" + update.key().name() + "/" + new Date().getTime());

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
                    int len;
                    byte[] buffer = new byte[8192];

                    GcsOutputChannel outputChannel = as.getApi().mGCS.createOrReplace(photoName, GcsFileOptions.getDefaultInstance());

                    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                        outputChannel.write(ByteBuffer.wrap(buffer, 0, len));
                    }

                    outputChannel.close();

                    photoUploaded = true;
                }
                else if (Config.PARAM_MESSAGE.equals(item.getFieldName())) {
                    message = Streams.asString(stream, "UTF-8");
                }
                else if (Config.PARAM_THING.equals(item.getFieldName())) {
                    thingId = Streams.asString(stream, "UTF-8");
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

        return new EarthViewer(as).getViewForEntityOrThrow(update).toJson();
    }

    private String editUpdate(EarthAs as, String updateId) {
        EarthThing update = new EarthStore(as).get(updateId);

        GcsFilename photoName = new GcsFilename(as.getApi().mAppIdentityService.getDefaultGcsBucketName(), "earth/thing/photo/" + update.key().name() + "/" + new Date().getTime());

        String message = null;
        boolean photoUploaded = update.getBoolean(EarthField.PHOTO);

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

                    GcsOutputChannel outputChannel = as.getApi().mGCS.createOrReplace(photoName, GcsFileOptions.getDefaultInstance());

                    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                        outputChannel.write(ByteBuffer.wrap(buffer, 0, len));
                    }

                    outputChannel.close();

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

        return new EarthViewer(as).getViewForEntityOrThrow(update).toJson();
    }
}
