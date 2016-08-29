package com.queatz.snappy;

import com.google.api.client.util.Base64;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;

import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 8/27/16.
 */

public class ImageWorker extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String thingId = req.getParameter("thing");

        EarthAs as = new EarthAs();

        GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        AppIdentityService appIdentityService = AppIdentityServiceFactory.getAppIdentityService();
        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        String fileName = ApiUtil.getFileName(gcsService, appIdentityService, "earth/thing/photo/" + thingId + "/");

        GcsFilename gcsFilename = new GcsFilename(appIdentityService.getDefaultGcsBucketName(), fileName);
        GcsInputChannel gcsInputChannel = gcsService.openReadChannel(gcsFilename, 0);

        Image image = ImagesServiceFactory.makeImage(IOUtils.toByteArray(Channels.newInputStream(gcsInputChannel)));

        float aspect = (float) image.getWidth() / (float) image.getHeight();

        imagesService.applyTransform(ImagesServiceFactory.makeResize(2, 2), image);

        EarthStore earthStore = new EarthStore(as);

        earthStore.save(earthStore.edit(earthStore.get(thingId))
                .set(EarthField.PLACEHOLDER, Base64.encodeBase64String(image.getImageData()))
                .set(EarthField.ASPECT_RATIO, aspect));
    }

//    public static int rgb(int red, int green, int blue) {
//        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
//    }
}
