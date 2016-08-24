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
import com.queatz.snappy.team.Team;
import com.queatz.snappy.util.Json;
import com.queatz.snappy.util.TimeUtil;

import java.util.Date;

/**
 * Created by jacob on 10/18/15.
 */
public class PartyPushHandler extends PushHandler {
    public PartyPushHandler(Team team) {
        super(team);
    }

    public void got(JsonObject push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        String name = push.get("name").getAsString();
        String id = push.get("id").getAsString();
        Date date = Json.from(push.get("date").getAsString(), Date.class);
        String firstName = push.getAsJsonObject("host").get("firstName").getAsString();

        builder = team.push.newNotification()
                .setContentTitle(String.format(team.context.getString(R.string.party_by_person), name, firstName))
                .setContentText(String.format(team.context.getString(R.string.party_starts_at), TimeUtil.cuteDate(date, true)))
                .setPriority(Notification.PRIORITY_LOW)
                .setDefaults(Notification.DEFAULT_LIGHTS);

        resultIntent = new Intent(team.context, Main.class);
        pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= 20) {
            resultIntent = new Intent(team.context, Background.class);
            Bundle extras = new Bundle();
            extras.putString(Config.EXTRA_ACTION, Config.EXTRA_ACTION_JOIN_REQUEST);
            extras.putString(Config.EXTRA_PARTY_ID, id);
            resultIntent.putExtras(extras);

            pendingIntent = PendingIntent.getService(team.context, 0, resultIntent, 0);

            builder.addAction(new NotificationCompat.Action(0, team.context.getString(R.string.interested), pendingIntent));
        }

        if(Build.VERSION.SDK_INT >= 21) {
            builder
                    .setColor(team.context.getResources().getColor(R.color.red))
                    .setCategory(Notification.CATEGORY_EVENT);
        }

        team.push.show("party/" + id, builder.build());
    }
}
