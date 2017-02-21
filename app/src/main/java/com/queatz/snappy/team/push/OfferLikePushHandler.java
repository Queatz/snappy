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

/**
 * Created by jacob on 8/2416.
 */
public class OfferLikePushHandler extends PushHandler {
    public OfferLikePushHandler(Team team) {
        super(team);
    }

    public void got(JsonObject push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        String firstName = push.getAsJsonObject("source").get("firstName").getAsString();
        String personId = push.getAsJsonObject("source").get("id").getAsString();

        boolean want = false;
        String text = null;

        if (push.has("target")) {
            JsonObject wantJsonObject = push.getAsJsonObject("target");
            want = wantJsonObject.has("want") && wantJsonObject.get("want").getAsBoolean();
            text = wantJsonObject.get("name").getAsString();
        }

        String message;


        if (text == null) {
            // Deprecate
            message = team.context.getString(R.string.liked_your_offer);
        } else {
            message = team.context.getString(
                    want ? R.string.liked_that_you_want : R.string.liked_that_you_offer,
                    text
            );
        }

        builder = team.push.newNotification()
                .setContentTitle(firstName)
                .setContentText(message);

        resultIntent = new Intent(team.context, Person.class);
        Bundle extras = new Bundle();
        extras.putString(Config.EXTRA_PERSON_ID, personId);
        resultIntent.putExtras(extras);
        pendingIntent = team.push.newIntentWithStack(resultIntent);

        builder.setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= 21) {
            builder
                    .setColor(team.context.getResources().getColor(R.color.red))
                    .setCategory(Notification.CATEGORY_SOCIAL);
        }

        team.push.show("liker/" + personId + "/offer", builder.build());
    }
}
