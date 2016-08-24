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
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 10/18/15.
 */
public class UpdatePushHandler extends PushHandler {
    public UpdatePushHandler(Team team) {
        super(team);
    }

    public void got(JsonObject push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        boolean photo = push.get("photo").getAsBoolean();
        String firstName = push.getAsJsonObject("person").get("firstName").getAsString();
        String personId = push.getAsJsonObject("person").get("id").getAsString();

        String message = photo ? team.context.getString(R.string.added_a_new_photo) :
                team.context.getString(R.string.posted_an_update);

        builder = team.push.newNotification()
                .setContentTitle(firstName)
                .setContentText(message);

        resultIntent = new Intent(team.context, Person.class);
        Bundle extras = new Bundle();
        extras.putString("person", personId);
        resultIntent.putExtras(extras);
        builder.setContentIntent(team.push.newIntentWithStack(resultIntent));

        if(Build.VERSION.SDK_INT >= 21) {
            builder
                    .setColor(team.context.getResources().getColor(R.color.red))
                    .setCategory(Notification.CATEGORY_SOCIAL);
        }

        team.push.show("person/" + personId + "/upto", builder.build());
    }
}
