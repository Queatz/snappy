package com.queatz.snappy.team.push;

import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 12/5/15.
 */
public class OfferPushHandler extends PushHandler {
    public OfferPushHandler(Team team) {
        super(team);
    }

    public void got(JsonObject push) {
        NotificationCompat.Builder builder;
        Intent resultIntent;
        Bundle extras;

        String id = push.get("id").getAsString();
        String details = push.get("details").getAsString();
        String firstName = push.getAsJsonObject("person").get("firstName").getAsString();
        String personId = push.getAsJsonObject("person").get("id").getAsString();

        builder = team.push.newNotification()
                .setContentTitle(team.context.getString(R.string.new_offer))
                .setContentText(team.context.getString(R.string.offer_by_person, details, firstName));

        resultIntent = new Intent(team.context, Person.class);
        extras = new Bundle();
        extras.putString("person", personId);
        resultIntent.putExtras(extras);
        builder.setContentIntent(team.push.newIntentWithStack(resultIntent));

        if(Build.VERSION.SDK_INT >= 21) {
            builder
                    .setColor(team.context.getResources().getColor(R.color.red))
                    .setCategory(Notification.CATEGORY_PROMO);
        }

        team.push.show("offer/" + id + "/new", builder.build());
    }
}
