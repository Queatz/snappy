package com.queatz.snappy.api;

import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;

import java.io.IOException;

/**
 * Created by jacob on 2/8/15.
 */

public class Example extends Api.Path {
    public Example(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case GET:
                get();

                break;
            default:
                die("example - bad method");
        }
    }

    private void get() throws IOException {
        response.getWriter().write("example");
    }
}
