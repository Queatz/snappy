package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;

/**
 * Created by jacob on 10/18/15.
 */
public class FollowPushHandler extends PushHandler {
    public FollowPushHandler(Team team) {
        super(team);
    }

    public void got(JsonObject push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        JsonObject source = push.get("source").getAsJsonObject();

        builder = team.push.newNotification()
                .setContentTitle(source.get("firstName").getAsString())
                .setContentText(team.context.getString(R.string.started_following_you));

        resultIntent = new Intent(team.context, Person.class);
        Bundle extras = new Bundle();
        extras.putString(Config.EXTRA_PERSON_ID, source.get(Thing.ID).getAsString());
        resultIntent.putExtras(extras);
        pendingIntent = team.push.newIntentWithStack(resultIntent);

        builder.setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= 21) {
            builder
                    .setColor(team.context.getResources().getColor(R.color.red))
                    .setCategory(Notification.CATEGORY_SOCIAL);
        }

        team.push.show("follow", builder.build());
    }
}
