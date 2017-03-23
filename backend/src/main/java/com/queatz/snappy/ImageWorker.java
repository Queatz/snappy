package com.queatz.snappy;

import com.image.SnappyImage;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

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

        SnappyImage snappyImage = new SnappyImage();

        String fileName = "earth/thing/photo/" + thingId;

        InputStream inputStream = snappyImage.openInputStream(fileName, 2, 2);
        float aspect = snappyImage.getAspectRatio(fileName);

        EarthStore earthStore = new EarthStore(as);

        earthStore.save(earthStore.edit(earthStore.get(thingId))
                .set(EarthField.PLACEHOLDER, Base64.encodeBase64String(IOUtils.toByteArray(inputStream)))
                .set(EarthField.ASPECT_RATIO, aspect));
    }
}
