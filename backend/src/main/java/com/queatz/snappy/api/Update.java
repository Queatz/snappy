package com.queatz.snappy.api;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 9/12/15.
 */
public class Update implements Api.Path {
    Api api;

    public Update(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                String updateId = path.get(0);

                if(path.size() != 2 || !Config.PATH_PHOTO.equals(path.get(1))) {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "update - bad path");
                }

                int size;

                try {
                    size = Integer.parseInt(req.getParameter(Config.PARAM_SIZE));
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
                    throw new PrintingError(Api.Error.NOT_FOUND);
                }

                ImagesService imagesService = ImagesServiceFactory.getImagesService();
                ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withGoogleStorageFileName(
                        "/gs/" + api.mAppIdentityService.getDefaultGcsBucketName() + "/" + fileName).imageSize(size);
                String photoUrl = imagesService.getServingUrl(servingUrlOptions);

                resp.sendRedirect(photoUrl);

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "update - bad method");
        }
    }
}
