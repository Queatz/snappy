package com.queatz.snappy.api;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;

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
    public void call() throws IOException, PrintingError {
        switch (method) {
            case GET:
                if(path.size() != 2) {
                    die("update - bad path");
                }

                String updateId = path.get(0);

                switch (path.get(1)) {
                    case Config.PATH_PHOTO:
                        getPhoto(updateId);
                        break;
                    default:
                        die("update - bad path");
                }

                break;
            default:
                die("update - bad method");
        }
    }

    private void getPhoto(String updateId) throws IOException, PrintingError {
        int size;

        try {
            size = Integer.parseInt(request.getParameter(Config.PARAM_SIZE));
        }
        catch (NumberFormatException e) {
            size = 200;
        }

        ListOptions options = new ListOptions.Builder().setPrefix("upto/photo/" + updateId + "/").setRecursive(false).build();
        ListResult list = api.mGCS.list(api.mAppIdentityService.getDefaultGcsBucketName(), options);

        Date lastModified = new Date(0);
        String fileName = null;
        while (list.hasNext()) {
            ListItem item = list.next();
            if(!item.isDirectory() && lastModified.before(item.getLastModified())) {
                lastModified = item.getLastModified();
                fileName = item.getName();
            }
        }

        if(fileName == null) {
            notFound();
        }

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withGoogleStorageFileName(
                "/gs/" + api.mAppIdentityService.getDefaultGcsBucketName() + "/" + fileName).imageSize(size);
        String photoUrl = imagesService.getServingUrl(servingUrlOptions);

        response.sendRedirect(photoUrl);

    }
}
