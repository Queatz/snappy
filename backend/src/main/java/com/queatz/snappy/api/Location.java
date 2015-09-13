package com.queatz.snappy.api;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 8/20/15.
 */
public class Location implements Api.Path {
    Api api;

    public Location(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case PUT:
                if(path.size() < 1) {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "location - bad path");
                }

                Document location = Search.getService().get(Search.Type.LOCATION, path.get(0));

                if(location == null) {
                    throw new PrintingError(Api.Error.NOT_FOUND);
                }

                if(path.size() != 2) {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "location - bad path");
                }

                if(Config.PATH_PHOTO.equals(path.get(1))) {
                    GcsFilename photoName = new GcsFilename(api.mAppIdentityService.getDefaultGcsBucketName(), "location/photo/" + location.getId() + "/" + new Date().getTime());

                    boolean allGood = false;

                    try {
                        ServletFileUpload upload = new ServletFileUpload();
                        FileItemIterator iterator = upload.getItemIterator(req);
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
                    }
                    catch (FileUploadException e) {
                        Logger.getLogger(Config.NAME).severe(e.toString());
                        throw new PrintingError(Api.Error.SERVER_ERROR, "location photo - couldn't upload because " + e);
                    }

                    if(!allGood)
                        throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "location photo - not all good");

                    resp.getWriter().write(Boolean.toString(true));
                }

                break;
            case GET:
                if(path.size() != 2) {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "location - bad path");
                }

                int size;

                try {
                    size = Integer.parseInt(req.getParameter(Config.PARAM_SIZE));
                }
                catch (NumberFormatException e) {
                    size = 200;
                }

                ListOptions options = new ListOptions.Builder().setPrefix("location/photo/" + path.get(0) + "/").setRecursive(false).build();
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
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "location - bad method");
        }
    }
}
