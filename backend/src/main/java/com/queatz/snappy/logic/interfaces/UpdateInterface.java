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
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.LikeEditor;
import com.queatz.snappy.logic.editors.UpdateEditor;
import com.queatz.snappy.logic.eventables.LikeEvent;
import com.queatz.snappy.logic.exceptions.LogicException;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.logic.views.LikeView;
import com.queatz.snappy.logic.views.UpdateView;
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

    final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    final LikeEditor likeEditor = EarthSingleton.of(LikeEditor.class);
    final UpdateEditor updateEditor = EarthSingleton.of(UpdateEditor.class);
    final EarthUpdate earthUpdate = EarthSingleton.of(EarthUpdate.class);

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

                String like = as.getRequest().getParameter(Config.PARAM_LIKE);

                if (Boolean.toString(true).equals(like)) {
                    return likeUpdate(as, updateId);
                }
                break;
        }

        throw new NothingLogicResponse("update - bad path");
    }

    private String likeUpdate(EarthAs as, String updateId) {
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        Entity poster = earthStore.get(updateId);
        Entity like = likeEditor.newLike(as.getUser(), poster);

        // XXX TODO authorize && not my own like
        earthUpdate.send(new LikeEvent(like))
                .to(poster.getKey(EarthField.SOURCE));

        return new LikeView(like).setLocalId(localId).toJson();
    }

    private String getLikers(EarthAs as, String updateId) {
        List<Entity> likers = earthStore.find(EarthKind.LIKE_KIND, EarthField.TARGET, earthStore.key(updateId));

        return new EntityListView(likers).toJson();
    }

    private String getPhoto(EarthAs as, String updateId) {
        int size;

        try {
            size = Integer.parseInt(as.getRequest().getParameter(Config.PARAM_SIZE));
        } catch (NumberFormatException e) {
            size = 200;
        }

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
                    "/gs/" + as.getApi().mAppIdentityService.getDefaultGcsBucketName() + "/" + fileName).imageSize(size);
            String photoUrl = imagesService.getServingUrl(servingUrlOptions);

            as.getResponse().sendRedirect(photoUrl);
        } catch (IOException e) {
            throw new LogicException("update photo - io error");
        }

        return null;
    }

    private String postUpdate(EarthAs as) {
        Entity update = updateEditor.stageUpdate(as.getUser());
        GcsFilename photoName = new GcsFilename(as.getApi().mAppIdentityService.getDefaultGcsBucketName(), "earth/thing/photo/" + update.key().name() + "/" + new Date().getTime());

        String message = null;
        boolean photoUploaded = false;
        String thingId = null;

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
            }
        }
        catch (FileUploadException | IOException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("upto photo - couldn't upload because: " + e);
        }

        if (thingId == null) {
            throw new NothingLogicResponse("post update - no thing");
        }

        if (message == null && !photoUploaded) {
            throw new NothingLogicResponse("post update - nothing to post");
        }

        update = updateEditor.updateWith(update, earthStore.get(thingId), message, photoUploaded);

        // XXX TODO post to followers of hub
        //Push.getService().sendToFollowers(as.getUser().key().name(), new PushSpec<>(Config.PUSH_ACTION_NEW_UPTO, update));

        return new UpdateView(update).toJson();
    }

}
