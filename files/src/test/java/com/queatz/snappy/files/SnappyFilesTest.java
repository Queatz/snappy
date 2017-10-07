package com.queatz.snappy.files;

import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.OutputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by jacob on 6/4/17.
 */
public class SnappyFilesTest {

    @Test
    public void testSaveAndReadFile() throws Exception {
        SnappyFiles snappyFiles = new SnappyFiles();

        byte[] raw = new byte[] {1, 2, 3, 4, 5};

        OutputStream outputStream = snappyFiles.openOutputStream("test");

        assertNotNull(outputStream);

        outputStream.write(raw);
        outputStream.close();

        InputStream inputStream = snappyFiles.openInputStream("test");

        assertNotNull(inputStream);

        byte[] readBytes = new byte[raw.length];

        int read = inputStream.read(readBytes);

        assertEquals(read, raw.length);
        assertEquals(raw, readBytes);
    }

}