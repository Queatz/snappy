package com.image;

import org.testng.annotations.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import static org.testng.Assert.*;

/**
 * Created by jacob on 4/1/17.
 */
public class SnappyImageTest {
    @Test
    public void testSaveAndReadImage() throws Exception {
        SnappyImage snappyImage = new SnappyImage();

        byte[] imageBytes = getExampleJpg();

        OutputStream outputStream = snappyImage.openOutputStream("test");

        assertNotNull(outputStream);

        outputStream.write(imageBytes);
        outputStream.close();

        InputStream inputStream = snappyImage.openInputStream("test", 0, 0);

        assertNotNull(inputStream);

        byte[] readBytes = new byte[imageBytes.length];

        int read = inputStream.read(readBytes);

        assertEquals(read, imageBytes.length);
        assertEquals(imageBytes, readBytes);
    }

    @Test
    public void testGetAspectRatio() throws Exception {
        SnappyImage snappyImage = new SnappyImage();

        byte[] imageBytes = getExampleJpg();

        OutputStream outputStream = snappyImage.openOutputStream("test");

        assertNotNull(outputStream);

        outputStream.write(imageBytes);
        outputStream.close();

        float aspect = snappyImage.getAspectRatio("test");
        assertEquals(aspect, 1f);
    }

    @Test
    public void testOpenScaledImage() throws Exception {
        SnappyImage snappyImage = new SnappyImage();

        byte[] imageBytes = getExampleJpg();

        OutputStream outputStream = snappyImage.openOutputStream("test");

        assertNotNull(outputStream);

        outputStream.write(imageBytes);
        outputStream.close();

        InputStream inputStream = snappyImage.openInputStream("test", 8, 0);

        assertNotNull(inputStream);

        BufferedImage scaledImage = ImageIO.read(inputStream);

        assertEquals(scaledImage.getWidth(), 8);
        assertEquals(scaledImage.getHeight(), 8);
    }

    private byte[] getExampleJpg() throws IOException {
        FileInputStream inputStream = new FileInputStream(new File("image/src/test/assets/test.jpg"));
        int total = inputStream.available();
        byte[] bytes = new byte[total];
        int read = inputStream.read(bytes);

        assertEquals(read, total);

        return bytes;
    }
}