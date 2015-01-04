package com.queatz.snappy.team;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;
import com.queatz.snappy.R;
import com.queatz.snappy.ui.MiniMenu;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by jacob on 11/23/14.
 */

public class Action {
    public Team team;

    public Action(Team t) {
        team = t;
    }

    public void openMinimenu(View source) {
        ((MiniMenu) team.view.findViewById(R.id.miniMenu)).show();
    }

    public boolean uploadUpto(Uri image, String location) {
        RequestParams params = new RequestParams();

        try {
            params.put("photo", team.context.getContentResolver().openInputStream(image));
            params.put("location", location);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

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

        return true;
    }
}
