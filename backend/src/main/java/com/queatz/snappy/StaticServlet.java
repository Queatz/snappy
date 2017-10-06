package com.queatz.snappy;

import com.google.common.base.Joiner;
import com.queatz.snappy.api.PrintingError;
import com.queatz.snappy.api.Error;
import com.queatz.snappy.shared.Config;

import org.omnifaces.servlet.FileServlet;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 4/1/17.
 */

public class StaticServlet extends FileServlet {
    @Override
    protected File getFile(HttpServletRequest request) throws IllegalArgumentException {
        String[] path;

        try {
            path = URLDecoder.decode(request.getRequestURI(), "UTF-8").split("/");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if(path.length < 3) {
            throw new PrintingError(Error.NOT_FOUND, "bad request length");
        }

        if(!Config.PATH_RAW.equals(path[1])) {
            throw new PrintingError(Error.NOT_FOUND, "bad request path");
        }

        String file = Joiner.on("/").join(Arrays.asList(path).subList(2, path.length));
        return Paths.get(Config.VILLAGE_FILES_DIR, file).toFile();
    }
}
