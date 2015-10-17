package com.queatz.snappy.api;

import com.queatz.snappy.service.Api;

/**
 * Created by jacob on 2/8/15.
 */

public class Pirate extends Api.Path {
    public Pirate(Api api) {
        super(api);
    }

    @Override
    public void call() {
        ok("yarr!");
    }
}