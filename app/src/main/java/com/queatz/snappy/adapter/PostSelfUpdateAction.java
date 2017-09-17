package com.queatz.snappy.adapter;

import android.location.Location;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.AuthenticatedAction;
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.team.actions.UpdateThings;
import com.queatz.snappy.util.Json;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class PostSelfUpdateAction extends AuthenticatedAction {

    private final Uri photo;
    private final String message;
    private final Location location;
    private final List<DynamicRealmObject> with;
    private final boolean isGoing;

    public PostSelfUpdateAction(Uri photo, String message) {
        this(photo, message, null, null, false);
    }

    public PostSelfUpdateAction(@Nullable final Uri photo,
                                @Nullable final String message,
                                @Nullable final Location location,
                                @Nullable final List<DynamicRealmObject> with,
                                final boolean isGoing) {
        this.photo = photo;
        this.message = message;
        this.location = location;
        this.with = with;
        this.isGoing = isGoing;
    }

    @Override
    public void whenAuthenticated() {
        RequestParams params = new RequestParams();

        if (photo == null && message == null && with == null) {
            return;
        }

        try {
            params.put(Config.PARAM_IN, getTeam().auth.getUser());

            if (photo != null) {
                params.put(Config.PARAM_PHOTO, getTeam().context.getContentResolver().openInputStream(photo), photo.getPath());
            }

            if (message != null) {
                params.put(Config.PARAM_MESSAGE, message);
            }

            if (location != null) {
                params.put(Config.PARAM_LATITUDE, location.getLatitude());
                params.put(Config.PARAM_LONGITUDE, location.getLongitude());
            }

            if (with != null) {
                List<String> withIds = new ArrayList<>();

                for (DynamicRealmObject person : with) {
                    withIds.add(person.getString(Thing.ID));
                }

                params.put(Config.PARAM_WITH, Json.to(withIds));
                params.put(Config.PARAM_GOING, isGoing);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // The server expects this whether or not there is an image being uploaded
        params.setForceMultipartEntityContentType(true);

        getTeam().api.post(Config.PATH_EARTH + "?kind=update&in=" + getTeam().auth.getUser(), params, new Api.Callback() {
            @Override
            public void success(String response) {
                to(new UpdateThings(response));

                // If location is null, then probably shared to Village from an external source
                if (location == null) {
                    to(new OpenProfileAction(getTeam().auth.me()));
                }
            }

            @Override
            public void fail(String response) {
                Toast.makeText(getTeam().context, "Couldn't post update", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
