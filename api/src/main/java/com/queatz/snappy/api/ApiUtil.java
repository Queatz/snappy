package com.queatz.snappy.api;

import com.image.SnappyImage;
import com.queatz.snappy.files.SnappyFiles;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Shared;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.queatz.snappy.files.SnappyFiles.FILES_POOL;

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
     * @param snappyImage The SnappyImage instance to use
     * @param request The raw request
     * @param response The raw response
     * @return If the get was successful
     * @throws IOException
     */
    public static boolean getPhoto(String thingId, SnappyImage snappyImage, HttpServletRequest request, HttpServletResponse response) throws IOException {
        int size;

        String fileName = Config.PHOTO_FILES_BUCKET + thingId;

        try {
            size = Integer.parseInt(request.getParameter(Config.PARAM_SIZE));
        } catch (NumberFormatException e) {
            size = 200;
        }

        size = Math.min(1600, size);

        response.sendRedirect(snappyImage.getServingUrl(fileName, size));

        return true;
    }

    /**
     * Function to put a photo in a GCS instance.
     *
     * @param thingId The file name
     * @param snappyImage The SnappyImage instance to use
     * @param request The request object
     * @return If the save was successful
     * @throws IOException
     */
    public static boolean putPhoto(String thingId, SnappyImage snappyImage, HttpServletRequest request) throws IOException {
        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(request);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    return putPhoto(thingId, item.getName(), snappyImage, item);
                }
            }
        } catch (FileUploadException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
        }

        return false;
    }

    public static boolean putPhoto(String thingId, String name, SnappyImage snappyImage, FileItemStream item) throws IOException {
        return putPhotoRaw(Config.PHOTO_FILES_BUCKET + thingId, name, snappyImage, item);
    }

    public static boolean putPhotoRaw(String photoName, String name, SnappyImage snappyImage, FileItemStream item) throws IOException {
        int len;
        byte[] buffer = new byte[8192];

        InputStream stream = item.openStream();
        OutputStream outputChannel = snappyImage.openOutputStream(photoName, name);

        if (outputChannel == null) {
            return false;
        }

        while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
            outputChannel.write(buffer, 0, len);
        }

        outputChannel.close();
        stream.close();

        return true;
    }

    public static String putFile(SnappyFiles snappyFiles, FileItemStream item) throws IOException {
        int len;
        byte[] buffer = new byte[8192];

        String fileName = Shared.randomToken() + Shared.randomToken() + "-" + item.getName().replace("/", "");
        InputStream stream = item.openStream();
        OutputStream outputChannel = snappyFiles.openOutputStream(fileName, item.getName());

        if (outputChannel == null) {
            return null;
        }

        while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
            outputChannel.write(buffer, 0, len);
        }

        outputChannel.close();
        stream.close();

        return FILES_POOL + "/" + fileName;
    }

    public static String fileUrl(String fileName) {
        return Paths.get("/", Config.PATH_RAW, fileName).toString();
    }
}
