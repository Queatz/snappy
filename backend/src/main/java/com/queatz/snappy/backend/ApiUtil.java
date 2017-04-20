package com.queatz.snappy.backend;

import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * @param thingId Filename
     * @param api Api object with the GCS instance
     * @param request The raw request
     * @param response The raw response
     * @return If the get was successful
     * @throws IOException
     */
    public static boolean getPhoto(String thingId, Api api, HttpServletRequest request, HttpServletResponse response) throws IOException {
        int size;

        String fileName = "earth/thing/photo/" + thingId;

        try {
            size = Integer.parseInt(request.getParameter(Config.PARAM_SIZE));
        } catch (NumberFormatException e) {
            size = 200;
        }

        // Maximum supported by GCE
        size = Math.min(1600, size);

        response.sendRedirect(api.snappyImage.getServingUrl(fileName, size));

        return true;
    }

    /**
     * Function to put a photo in a GCS instance.
     *
     * @param thingId The file name
     * @param api The Api with the GCS instance
     * @param request The request object
     * @return If the save was successful
     * @throws IOException
     */
    public static boolean putPhoto(String thingId, Api api, HttpServletRequest request) throws IOException {
        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(request);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    return putPhoto(thingId, api, item);
                }
            }
        } catch (FileUploadException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
        }

        return false;
    }

    public static boolean putPhoto(String thingId, Api api, FileItemStream item) throws IOException {
        String photoName = "earth/thing/photo/" + thingId;

        int len;
        byte[] buffer = new byte[8192];

        InputStream stream = item.openStream();
        OutputStream outputChannel = api.snappyImage.openOutputStream(photoName);

        if (outputChannel == null) {
            return false;
        }

        while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
            outputChannel.write(buffer, 0, len);
        }

        outputChannel.close();

        return true;
    }
}
