package com.queatz.snappy.team;

import android.net.Uri;
import android.util.Log;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by jacob on 11/23/14.
 */
public class Action {
    public Team team;

    public Action(Team t) {
        team = t;
    }

    public void uploadUpto(Uri image, String location) {
        RequestParams params = new RequestParams();

        params.put("photo", image);
        params.put("location", location);

        team.api.post(Config.PATH_ME_UPTO, params, new Api.Callback() {
            @Override
            public void success(String response) {
                Log.d(Config.TAG, "yay new upto posted");
            }

            @Override
            public void fail(String response) {
                Log.e(Config.TAG, "error uploading new upto: " + response);
            }
        });
    }
}
