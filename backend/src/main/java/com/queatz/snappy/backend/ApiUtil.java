package com.queatz.snappy.backend;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utilities for server stuff.
 *
 * Created by jacob on 1/11/16.
 */
public class ApiUtil {
    /**
     * Function to read a photo from a GCS instance.
     *
     * @param prefix Filename
     * @param api Api object with the GCS instance
     * @param request The raw request
     * @param response The raw response
     * @return If the get was successful
     * @throws IOException
     */
    public static boolean getPhoto(String prefix, Api api, HttpServletRequest request, HttpServletResponse response) throws IOException {
        int size;

        try {
            size = Integer.parseInt(request.getParameter(Config.PARAM_SIZE));
        } catch (NumberFormatException e) {
            size = 200;
        }

        ListOptions options = new ListOptions.Builder().setPrefix(prefix).setRecursive(false).build();
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
            return false;
        }

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withGoogleStorageFileName(
                "/gs/" + api.mAppIdentityService.getDefaultGcsBucketName() + "/" + fileName).imageSize(size)
                .secureUrl(true);
        String photoUrl = imagesService.getServingUrl(servingUrlOptions);

        response.sendRedirect(photoUrl);

        return true;
    }

    /**
     * Function to put a photo in a GCS instance.
     *
     * @param name The file name
     * @param api The Api with the GCS instance
     * @param request The request object
     * @return If the save was successful
     * @throws IOException
     */
    public static boolean putPhoto(String name, Api api, HttpServletRequest request) throws IOException {
        GcsFilename photoName = new GcsFilename(api.mAppIdentityService.getDefaultGcsBucketName(), name);

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

                    return true;
                }
            }
        } catch (FileUploadException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
        }

        return false;
    }
}
