package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.queatz.snappy.Background;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.JoinLinkSpec;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jacob on 10/18/15.
 */
public class JoinPushHandler extends PushHandler {
    public JoinPushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<JoinLinkSpec> push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        switch (push.action) {
            case Config.PUSH_ACTION_JOIN_REQUEST:
                builder = team.push.newNotification()
                        .setContentTitle(push.body.person.firstName)
                        .setContentText(String.format(team.context.getString(R.string.requested_to_join_party), push.body.party.name));

                resultIntent = new Intent(team.context, Main.class);
                pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 20) {
                    resultIntent = new Intent(team.context, Background.class);
                    Bundle extras = new Bundle();
                    extras.putString(Config.EXTRA_ACTION, Config.EXTRA_ACTION_JOIN_ACCEPT);
                    extras.putString(Config.EXTRA_JOIN_ID, push.body.id);
                    resultIntent.putExtras(extras);

                    pendingIntent = PendingIntent.getService(team.context, 0, resultIntent, 0);

                    builder.addAction(new NotificationCompat.Action(0, team.context.getString(R.string.accept), pendingIntent));
                }

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_EVENT);
                }

                team.push.show("join/" + push.body.id + "/request", builder.build());

                break;
            case Config.PUSH_ACTION_JOIN_ACCEPTED:
                builder = team.push.newNotification()
                        .setContentTitle(push.body.party.name)
                        .setContentText(String.format(team.context.getString(R.string.request_accepted), TimeUtil.relDate(push.body.party.date)));

                resultIntent = new Intent(team.context, Main.class);
                pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_EVENT);
                }

                team.push.show("join/" + push.body.id + "/accept", builder.build());

                break;
        }

        fetch(push);
    }

    private void fetch(PushSpec<JoinLinkSpec> push) {
        switch (push.action) {
            case Config.PUSH_ACTION_JOIN_REQUEST:
                team.api.get(String.format(Config.PATH_JOIN_ID, push.body.id), new Api.Callback() {
                    @Override
                    public void success(String response) {
                        team.things.put(Join.class, response);
                    }

                    @Override
                    public void fail(String response) {

                    }
                });

                break;
            case Config.PUSH_ACTION_JOIN_ACCEPTED:
                team.api.get(String.format(Config.PATH_JOIN_ID, push.body.id), new Api.Callback() {
                    @Override
                    public void success(String response) {
                        team.things.put(Join.class, response);
                    }

                    @Override
                    public void fail(String response) {

                    }
                });

                break;
        }
    }
}
