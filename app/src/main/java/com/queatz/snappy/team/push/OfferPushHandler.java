package com.queatz.snappy.team.push;

import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.queatz.snappy.R;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.OfferSpec;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 12/5/15.
 */
public class OfferPushHandler extends PushHandler {
    public OfferPushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<OfferSpec> push) {
        NotificationCompat.Builder builder;
        Intent resultIntent;

        switch (push.action) {
            case Config.PUSH_ACTION_NEW_OFFER:
                builder = team.push.newNotification()
                        .setContentTitle(team.context.getString(R.string.new_offer))
                        .setContentText(team.context.getString(R.string.offer_by_person, push.body.details, push.body.person.firstName));

                resultIntent = new Intent(team.context, Person.class);
                Bundle extras = new Bundle();
                extras.putString("person", push.body.person.id);
                resultIntent.putExtras(extras);
                builder.setContentIntent(team.push.newIntentWithStack(resultIntent));

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_PROMO);
                }

                team.push.show("offer/" + push.body.id + "/new", builder.build());

                break;
        }
    }
}
