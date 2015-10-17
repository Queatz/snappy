package com.queatz.snappy.api;

import com.queatz.snappy.service.Api;

/**
 * Created by jacob on 2/8/15.
 */

public class Example extends Api.Path {
    public Example(Api api) {
        super(api);
    }

    @Override
    public void call() {
        switch (method) {
            case GET:
                get();

                break;
            default:
                die("example - bad method");
        }
    }

    private void get() {
        ok("example");
    }
}
