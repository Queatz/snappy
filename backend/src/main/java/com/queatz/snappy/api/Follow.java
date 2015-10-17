package com.queatz.snappy.api;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.things.FollowLinkSpec;

/**
 * Created by jacob on 2/19/15.
 */
public class Follow extends Api.Path {
    public Follow(Api api) {
        super(api);
    }

    @Override
    public void call() {
        switch (method) {
            case GET:
                if (path.size() != 1) {
                    die("follow - bad path");
                }

                getFollow(path.get(0));

                break;
            default:
                die("follow - bad method");
        }
    }

    private void getFollow(String followId) {
        ok(Datastore.get(FollowLinkSpec.class, followId));
    }
}
