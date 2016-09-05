package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;
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

        JsonArray with;

        if (push.has("with")) {
            with = push.getAsJsonArray("with");
        } else {
            with = new JsonArray();
        }

        String isAt = null;
        String isWith = null;

        for (int i = 0; i < with.size(); i++) {
            JsonObject w = with.get(i).getAsJsonObject();

            if ("hub".equals(w.get("kind").getAsString())) {
                if (isAt != null) {
                    Log.w(Config.LOG_TAG, "Update notification with more than 1 hub associated, skipping.");
                    continue;
                }

                isAt = w.get("name").getAsString();
            } else if ("person".equals(w.get("kind").getAsString())) {
                if (isAt != null) {
                    Log.w(Config.LOG_TAG, "Update notification with more than 1 person associated, skipping.");
                    continue;
                }

                isWith = w.get("name").getAsString();
            }
        }

        String message;

        if (isAt == null) {
            message = photo ? team.context.getString(R.string.added_a_new_photo) :
                    team.context.getString(R.string.posted_an_update);
        } else {
            message = team.context.getResources().getQuantityString(R.plurals.is_are_at, isWith == null ? 1 : 2, isAt);
        }

        if (isWith != null) {
            firstName = team.context.getString(R.string.and, firstName, isWith);
        }

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
