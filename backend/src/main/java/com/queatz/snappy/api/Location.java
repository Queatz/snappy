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
import com.queatz.snappy.backend.ApiUtil;
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
        if (!ApiUtil.getPhoto("location/photo/" + locationId + "/", api, request, response)) {
            notFound();
        }
    }

    private void putPhoto(String locationId) throws IOException {
        LocationSpec location = Datastore.get(LocationSpec.class, locationId);

        if (location == null) {
            notFound();
        }

        if (!ApiUtil.putPhoto("location/photo/" + location.id + "/" + new Date().getTime(), api, request)) {
            die("location photo - not all good");
        }

        ok(true);
    }
}
