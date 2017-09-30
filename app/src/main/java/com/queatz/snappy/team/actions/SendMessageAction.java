package com.queatz.snappy.team.actions;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;
import com.queatz.snappy.util.LocalState;

import java.io.FileNotFoundException;
import java.util.Date;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class SendMessageAction extends AuthenticatedAction {

    final DynamicRealmObject to;
    final String message;
    final Uri photo;

    public SendMessageAction(@NonNull final DynamicRealmObject to, @Nullable final String message, @Nullable final Uri photo) {
        this.to = to;
        this.message = message;
        this.photo = photo;
    }

    @Override
    public void whenAuthenticated() {
        if (photo == null && message == null) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final String localId = Util.createLocalId();

                RequestParams params = new RequestParams();
                params.put(Config.PARAM_LOCAL_ID, localId);

                if (message != null) {
                    params.put(Config.PARAM_MESSAGE, message);
                }

                if (photo != null) try {
                    params.put(Config.PARAM_PHOTO, getTeam().context.getContentResolver().openInputStream(photo), photo.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }

                params.setForceMultipartEntityContentType(true);

                getTeam().realm.beginTransaction();
                DynamicRealmObject o = getTeam().realm.createObject("Thing");
                o.setString(Thing.KIND, ThingKinds.MESSAGE);
                o.setString(Thing.ID, localId);
                o.setObject(Thing.FROM, getTeam().auth.me());
                o.setObject(Thing.TO, to);

                if (message != null) {
                    o.setString(Thing.MESSAGE, message);
                }

                o.setDate(Thing.DATE, new Date());

                getTeam().realm.commitTransaction();

                getTeam().local.updateRecentsForMessage(o);

                getTeam().api.post(Config.PATH_EARTH + "/" + to.getString(Thing.ID) + "/" + Config.PATH_MESSAGE, params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        to(new UpdateThings(response));
                    }

                    @Override
                    public void fail(String response) {
                        // Reverse local modifications after retrying
                        Toast.makeText(getTeam().context, R.string.message_not_sent, Toast.LENGTH_SHORT).show();

                        DynamicRealmObject message = getTeam().realm.where("Thing")
                                .equalTo(Thing.ID, localId)
                                .findFirst();

                        if (message != null) {
                            getTeam().realm.beginTransaction();
                            message.set(Thing.LOCAL_STATE, LocalState.UNSYNCED);
                            getTeam().realm.commitTransaction();
                        }
                    }
                });

                return null;
            }
        }.execute();
    }
}
