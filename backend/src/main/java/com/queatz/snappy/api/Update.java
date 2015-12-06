package com.queatz.snappy.api;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.FollowLinkSpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.UpdateLikeSpec;
import com.queatz.snappy.shared.things.UpdateSpec;

import java.io.IOException;
import java.util.Date;

/**
 * Created by jacob on 9/12/15.
 */
public class Update extends Api.Path {
    public Update(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        String updateId;

        switch (method) {
            case GET:
                if (path.size() != 2) {
                    die("update - bad path");
                }

                updateId = path.get(0);

                switch (path.get(1)) {
                    case Config.PATH_LIKERS:
                        getLikers(updateId);
                        break;
                    case Config.PATH_PHOTO:
                        getPhoto(updateId);
                        break;
                    default:
                        die("update - bad path");
                }

                break;
            case POST:
                if (path.size() != 1) {
                    die("update - bad path");
                }

                updateId = path.get(0);

                String like = request.getParameter(Config.PARAM_LIKE);

                if (Boolean.toString(true).equals(like)) {
                    likeUpdate(updateId);
                }

                break;
            default:
                die("update - bad method");
        }
    }

    private void getLikers(String updateId) {
        UpdateSpec update= Datastore.get(UpdateSpec.class, updateId);

        if (update == null) {
            notFound();
        }

        ok(Datastore.get(UpdateLikeSpec.class).filter("targetId", update).list());
    }

    private void getPhoto(String updateId) throws IOException {
        int size;

        try {
            size = Integer.parseInt(request.getParameter(Config.PARAM_SIZE));
        } catch (NumberFormatException e) {
            size = 200;
        }

        ListOptions options = new ListOptions.Builder().setPrefix("upto/photo/" + updateId + "/").setRecursive(false).build();
        ListResult list = api.mGCS.list(api.mAppIdentityService.getDefaultGcsBucketName(), options);

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
            notFound();
        }

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withGoogleStorageFileName(
                "/gs/" + api.mAppIdentityService.getDefaultGcsBucketName() + "/" + fileName).imageSize(size);
        String photoUrl = imagesService.getServingUrl(servingUrlOptions);

        response.sendRedirect(photoUrl);
    }

    private void likeUpdate(String updateId) {
        UpdateSpec update = Datastore.get(UpdateSpec.class, updateId);

        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        if (update == null) {
            notFound();
        }

        UpdateLikeSpec updateLike = Thing.getService().update.like(update, user);

        if (updateLike != null) {
            updateLike.localId = localId;

            if (!user.id.equals(Datastore.id(updateLike.sourceId))) {
                Push.getService().send(Datastore.id(update.personId), new PushSpec<>(Config.PUSH_ACTION_LIKE_UPDATE, updateLike));
            }
        }

        ok(updateLike);
    }
}
