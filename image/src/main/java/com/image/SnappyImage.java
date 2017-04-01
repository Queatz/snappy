package com.image;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.queatz.snappy.shared.Gateway;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Save and load images at any size.
 */
public class SnappyImage {

    private final static String filePoolPath = "images-pool/";
    private final static String IMAGES_DATABASE_COLLECTION = "Images";

    private ArangoDatabase database;
    private ArangoCollection collection;

    public SnappyImage() {
        init();
    }

    private void init() {
        database = new ArangoDB.Builder()
                .user(Gateway.ARANGO_USER)
                .password(Gateway.ARANGO_PASSWORD)
                .build()
                .db();

        try {
            database.createCollection(IMAGES_DATABASE_COLLECTION);
        } catch (ArangoDBException ignored) {
            // Whatever
        }

        collection = database.collection(IMAGES_DATABASE_COLLECTION);
    }

    /**
     * Opens a writable stream to store an image at path.
     * @param path The image path
     * @return The output stream to write to, or null if the path already exists
     */
    @Nullable
    public OutputStream openOutputStream(final String path) {
        return openOutputStream(path, null);
    }

    @Nullable
    private OutputStream openOutputStream(final String path, final Point size) {

        // Ensure pool folder exists
        File pool = new File(filePoolPath);

        if (!pool.exists()) {
            if (!pool.mkdirs()) {
                return null;
            }
        }

        // Write to file

        OutputStream outputStream;

        try {
            final File file = getNewFile();

            outputStream = new FileOutputStream(file) {
                @Override
                public void close() throws IOException {
                    super.close();
                    saveImage(path, file, size == null, size != null && size.getY() == 0f);
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Save attributes

        return outputStream;
    }

    /**
     * Reads and saves image details after successfully written to the pool.
     */
    private boolean saveImage(String path, File file, boolean original, boolean scaled) {
        SnappyImageMetadata metadata = new SnappyImageMetadata();

        metadata.path = path;
        metadata.file = file.getName();
        metadata.original = original;
        metadata.scaled = scaled;

        BufferedImage image;

        try {
            image = SnappyImageUtil.image(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (image == null) {
            return false;
        }

        metadata.width = image.getWidth();
        metadata.height = image.getHeight();

        collection.insertDocument(metadata);

        return true;
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
     * @return The aspect ratio, or -1 if unobtainable
     */
    public float getAspectRatio(@NotNull String path) {
        SnappyImageMetadata metadata = getImageMetadata(path, null);
        if (metadata == null) {
            return -1;
        }

        return (float) metadata.width / (float) metadata.height;
    }

    private float getAspectRatio(@NotNull SnappyImageMetadata metadata) {
        return (float) metadata.width / (float) metadata.height;
    }

    @Nullable
    private SnappyImageMetadata getImageMetadata(String path, Point size) {
        String sizeQuery = size == null ? "x.original == true" : "x.width == @width and " +
                (size.getY() == 0 ? "x.scaled == true" : "x.height == @height");
        String query = "for x in " + IMAGES_DATABASE_COLLECTION + " filter x.path == @path and " + sizeQuery + " return x";
        Map<String, Object> vars = new HashMap<>();
        vars.put("path", path);

        if (size != null) {
            vars.put("width", (int) size.getX());

            if (size.getY() != 0f) {
                vars.put("height", (int) size.getY());
            }
        }

        ArangoCursor<SnappyImageMetadata> cursor = database.query(query, vars, null, SnappyImageMetadata.class);

        if (cursor.hasNext()) {
            return cursor.next();
        } else {
            return null;
        }
    }

    /**
     * Opens a stream to read a scaled image
     *
     * @param path The image path
     * @param w The requested image width, or 0 to not resize
     * @param h The requested image height, or 0 to keep aspect
     * @return An image stream where the image can be read, or null of not found
     */
    public InputStream openInputStream(String path, int w, int h) {
        Point size = (w == 0) ? null : new Point(w, h);
        SnappyImageMetadata metadata = getImageMetadata(path, size);

        // Create image if it doesn't exist, based off the original size
        if (metadata == null) {
            if (size != null) {
                metadata = getImageMetadata(path, null);
            }

            if (metadata == null) {
                return null;
            }

            metadata = getScaledImageMetadata(metadata, w, h);

            if (metadata == null) {
                return null;
            }
        }

        File file = getFileFromName(metadata.file);

        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private SnappyImageMetadata getScaledImageMetadata(@NotNull SnappyImageMetadata metadata, int w, int h) {
        BufferedImage image;

        try {
            image = SnappyImageUtil.image(getFileFromName(metadata.file));
            image = SnappyImageUtil.scale(image, w, h > 0 ? h : (int) (w / getAspectRatio(metadata)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Point size = new Point(w, h);
        OutputStream outputStream = openOutputStream(metadata.path, size);

        try {
            SnappyImageUtil.write(outputStream, image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return getImageMetadata(metadata.path, size);
    }

    private File getFileFromName(String file) {
        return Paths.get(filePoolPath, file).toFile();
    }

    @NotNull
    private File getNewFile() throws IOException {
        File file = getFileFromName(getNewFileName());

        if (!file.createNewFile()) {
            throw new IOException("Could not create file");
        }

        return file;
    }

    @NotNull
    private String getNewFileName() {
        Random random = new Random();
        return String.valueOf(random.nextLong()) +
               String.valueOf(random.nextLong()) +
               String.valueOf(random.nextLong());
    }
}
