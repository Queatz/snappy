package com.image;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Save and load images at any size.
 */
public class SnappyImage {

    /**
     * Opens a writable stream to store an image at path.
     * @param path The image path
     * @return The output stream to write to
     */
    public OutputStream openOutputStream(String path) {
        return null;

        // save stream to file
        // save 'path' in ArangoDB with params:
        //   - aspect ratio
        //   - file name that was saved
        // make sure ArangoDB versioning is ON
    }

    /**
     * Returns a url where a scaled image can be accessed.
     * @param path The image path
     * @param size The requested image width, or 0 to not do scaling
     * @return A url where the image can be found
     */
    public String getServingUrl(String path, int size) {
        return null;

        // return path to raw file of size
        // if size is not generated yet, generate it, and then return
    }

    /**
     * Gets the aspect ratio of the image at path.
     * @param path The image path
     * @return The aspect ratio
     */
    public float getAspectRatio(String path) {
        return 0;

        // query ArangoDB for aspect ratio
    }

    /**
     * Opens a stream to read a scaled image
     *
     * @param path The image path
     * @param w The requested image width, or 0 to not resize
     * @param h The requested image height, or 0 to keep aspect
     * @return An image stream where the image can be read
     */
    public InputStream openInputStream(String path, int w, int h) {
        return null;

        // query ArangoDB for file path
        // if size is not generated, generate it
        // store and return raw file bytes
    }
}
