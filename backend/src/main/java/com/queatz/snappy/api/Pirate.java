package com.queatz.snappy.api;

import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;

import java.io.IOException;

/**
 * ARRR...
 *
 * Created by jacob on 2/8/15.
 */

public class Pirate extends Api.Path {
    public Pirate(Api api) {
        super(api);
    }

    @Override
    public void call() {
        try {
            response.getWriter().write("yarr!");
        } catch (IOException e) {
            throw new PrintingError(Api.Error.SERVER_ERROR, "yarr failed");
        }
    }
}