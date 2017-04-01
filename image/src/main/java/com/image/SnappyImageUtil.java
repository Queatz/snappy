package com.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * Created by jacob on 4/1/17.
 */

public class SnappyImageUtil {
    public static BufferedImage image(File file) throws IOException {
        return ImageIO.read(file);
    }

    public static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        if (dWidth == 0 || dWidth == imageToScale.getWidth()) {
            return imageToScale;
        }

        if (dHeight == 0) {
            dHeight = imageToScale.getHeight() * (dWidth / imageToScale.getWidth());
        }

        BufferedImage scaledImage = null;
        if (imageToScale != null) {
            scaledImage = new BufferedImage(dWidth, dHeight, imageToScale.getType());
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
            graphics2D.dispose();
        }
        return scaledImage;
    }

    public static void write(OutputStream outputStream, BufferedImage image) throws IOException {
        ImageIO.write(image, "jpg", outputStream);
        outputStream.close();
    }
}
