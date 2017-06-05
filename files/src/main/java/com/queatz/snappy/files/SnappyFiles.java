package com.queatz.snappy.files;

import com.queatz.snappy.shared.Config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

public class SnappyFiles {

    private final String filePoolPath;

    public SnappyFiles(@NotNull String poolName) {
        filePoolPath = Config.VILLAGE_FILES_DIR + poolName;
    }

    @Nullable
    public OutputStream openOutputStream(final String path) {
        return openOutputStream(path, null);
    }

    @Nullable
    public OutputStream openOutputStream(final String path, @Nullable final Runnable onCloseCallback) {

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
            final File file = getNewFile(path);

            outputStream = new FileOutputStream(file) {
                @Override
                public void close() throws IOException {
                    super.close();

                    if (onCloseCallback != null) {
                        onCloseCallback.run();
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outputStream;
    }

    /**
     * Opens a stream to read a file
     *
     * @param path The file path
     * @return A file stream where the file can be read, or null if not found
     */
    @Nullable
    public InputStream openInputStream(@NotNull String path) {
        File file = getFileFromName(path);

        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getFileFromName(String file) {
        return Paths.get(filePoolPath, file).toFile();
    }

    @NotNull
    private File getNewFile(@NotNull String path) throws IOException {
        File file = getFileFromName(path);

        if (!file.createNewFile()) {
            throw new IOException("Could not create file");
        }

        return file;
    }
}
