package com.queatz.snappy.team.actions;

import android.os.AsyncTask;

import com.queatz.branch.Branch;
import com.queatz.snappy.team.contexts.ActivityContext;

/**
 * Created by jacob on 9/16/17.
 */

public class UpdateThings extends Branch<ActivityContext> {
    public UpdateThings(final String response) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                me().getTeam().things.put(response);
            }
        });
    }
}
