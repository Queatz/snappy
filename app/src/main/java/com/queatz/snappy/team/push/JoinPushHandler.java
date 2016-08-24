package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.queatz.snappy.Background;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.util.Json;
import com.queatz.snappy.util.TimeUtil;

import java.util.Date;

/**
 * Created by jacob on 10/18/15.
 */
public class JoinPushHandler extends PushHandler {
    public JoinPushHandler(Team team) {
        super(team);
    }

    public void got(String action, JsonObject push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        String id = push.get("id").getAsString();
        String personName = push.getAsJsonObject("person").get("firstName").getAsString();
        String partyName = push.getAsJsonObject("party").get("name").getAsString();
        Date date = Json.from(push.getAsJsonObject("party").get("date").getAsString(), Date.class);

        switch (action) {
            case Config.PUSH_ACTION_JOIN_REQUEST:
                builder = team.push.newNotification()
                        .setContentTitle(personName)
                        .setContentText(String.format(team.context.getString(R.string.requested_to_join_party), partyName));

                resultIntent = new Intent(team.context, Main.class);
                pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 20) {
                    resultIntent = new Intent(team.context, Background.class);
                    Bundle extras = new Bundle();
                    extras.putString(Config.EXTRA_ACTION, Config.EXTRA_ACTION_JOIN_ACCEPT);
                    extras.putString(Config.EXTRA_JOIN_ID, id);
                    resultIntent.putExtras(extras);

                    pendingIntent = PendingIntent.getService(team.context, 0, resultIntent, 0);

                    builder.addAction(new NotificationCompat.Action(0, team.context.getString(R.string.accept), pendingIntent));
                }

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_EVENT);
                }

                team.push.show("join/" + id + "/request", builder.build());

                break;
            case Config.PUSH_ACTION_JOIN_ACCEPTED:
                builder = team.push.newNotification()
                        .setContentTitle(partyName)
                        .setContentText(String.format(team.context.getString(R.string.request_accepted), TimeUtil.relDate(date)));

                resultIntent = new Intent(team.context, Main.class);
                pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_EVENT);
                }

                team.push.show("join/" + id + "/accept", builder.build());

                break;
        }

        fetch(push);
    }

    private void fetch(JsonObject push) {
        team.api.get(Config.PATH_EARTH + "/" +  push.get("id").getAsString(), new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);
            }

            @Override
            public void fail(String response) {

            }
        });
    }
}
