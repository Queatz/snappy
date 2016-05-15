package com.queatz.snappy.logic.interfaces;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.LikeEditor;
import com.queatz.snappy.logic.exceptions.LogicException;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.logic.views.LikeView;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 5/9/16.
 */
public class UpdateInterface implements Interfaceable {

    final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    final LikeEditor likeEditor = EarthSingleton.of(LikeEditor.class);

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
            case 1:
                String updateId = as.getRoute().get(0);

                String like = as.getRequest().getParameter(Config.PARAM_LIKE);

                if (Boolean.toString(true).equals(like)) {
                    return likeUpdate(as, updateId);
                }
        }

        throw new NothingLogicResponse("update - bad path");
    }

    private String likeUpdate(EarthAs as, String updateId) {
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        Entity like = likeEditor.newLike(as.getUser(), earthStore.get(updateId));

        // XXX TODO authorize && not my own like
        Push.getService().send(as.getUser().key().name(), new PushSpec<>(Config.PUSH_ACTION_LIKE_UPDATE, like));

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
            ListOptions options = new ListOptions.Builder().setPrefix("upto/photo/" + updateId + "/").setRecursive(false).build();
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

}
