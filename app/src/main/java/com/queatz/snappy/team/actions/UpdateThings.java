package com.queatz.snappy.team.actions;

import android.os.AsyncTask;
import android.util.Log;

import com.queatz.branch.Branch;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.Things;
import com.queatz.snappy.team.contexts.ActivityContext;

import java.util.ArrayList;
import java.util.List;

import io.realm.DynamicRealmObject;
import io.realm.RealmList;

/**
 * Created by jacob on 9/16/17.
 */

public class UpdateThings extends Branch<ActivityContext> {

    private final String response;
    private String resultId = null;
    private List<String> resultIds = null;

    public UpdateThings(final String response) {
        this.response = response;
    }

    @Override
    protected void execute() {
        if (response == null || response.length() < 1) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (me() == null) {
                    return null;
                }

                switch (response.charAt(0)) {
                    case '{':
                        resultId = new Things(me().getTeam().realm()).put(response).getString(Thing.ID);
                        break;
                    case '[':
                        RealmList<DynamicRealmObject> results = new Things(me().getTeam().realm()).putAll(response);

                        resultIds = new ArrayList<>();

                        for (DynamicRealmObject result : results) {
                            resultIds.add(result.getString(Thing.ID));
                        }

                        break;
                    default:
                        Log.w(Config.LOG_TAG, "Cannot put things: " + response);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                emit(true);

                if (resultId != null) {
                    emit(me().getTeam().realm.where("Thing")
                            .equalTo(Thing.ID, resultId)
                    .findFirst());
                }

                if (resultIds != null) {
                    emit(me().getTeam().realm.where("Thing").in(Thing.ID, resultIds.toArray(new String[0])).findAll());
                }
            }
        }.execute();
    }
}
