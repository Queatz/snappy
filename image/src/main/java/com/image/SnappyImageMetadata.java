package com.image;

/**
 * Created by jacob on 4/1/17.
 */

public class SnappyImageMetadata {

    /**
     * The file path.  This is just a name to reference the file by.
     */
    public String path;

    /**
     * The actual file name on disk.
     */
    public String file;

    /**
     * The calculated image width.
     */
    public int width;

    /**
     * The calculated image height.
     */
    public int height;

    /**
     * Whether or not this is considered an original, non-scaled image.
     */
    public boolean original;

    /**
     * Whether or not this is considered an aspect-locked scaled version.
     */
    public boolean scaled;
}
