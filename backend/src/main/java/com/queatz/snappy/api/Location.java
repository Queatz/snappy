package com.queatz.snappy.api;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.LocationSpec;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by jacob on 8/20/15.
 */
public class Location extends Api.Path {
    public Location(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        switch (method) {
            case GET:
                if (path.size() != 2) {
                    die("location - bad path");
                }

                switch (path.get(1)) {
                    case Config.PATH_PHOTO:
                        getPhoto(path.get(0));
                        break;
                    default:
                        die("location - bad path");
                }

                break;
            case PUT:
                if (path.size() != 2) {
                    die("location - bad path");
                }

                switch (path.get(1)) {
                    case Config.PATH_PHOTO:
                        putPhoto(path.get(0));

                        break;
                    default:
                        die("location - bad path");
                }

                break;
            default:
                die("location - bad method");
        }
    }

    private void getPhoto(String locationId) throws IOException {
        int size;

        try {
            size = Integer.parseInt(request.getParameter(Config.PARAM_SIZE));
        } catch (NumberFormatException e) {
            size = 200;
        }

        ListOptions options = new ListOptions.Builder().setPrefix("location/photo/" + locationId + "/").setRecursive(false).build();
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

    private void putPhoto(String locationId) throws IOException {
        LocationSpec location = Datastore.get(LocationSpec.class, locationId);

        if (location == null) {
            notFound();
        }

        GcsFilename photoName = new GcsFilename(api.mAppIdentityService.getDefaultGcsBucketName(), "location/photo/" + location.id + "/" + new Date().getTime());

        boolean allGood = false;

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(request);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    int len;
                    byte[] buffer = new byte[8192];

                    GcsOutputChannel outputChannel = api.mGCS.createOrReplace(photoName, GcsFileOptions.getDefaultInstance());

                    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                        outputChannel.write(ByteBuffer.wrap(buffer, 0, len));
                    }

                    outputChannel.close();

                    allGood = true;

                    break;
                }
            }
        } catch (FileUploadException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
            error("location photo - couldn't upload because " + e);
        }

        if (!allGood) {
            die("location photo - not all good");
        }

        ok(true);
    }
}
