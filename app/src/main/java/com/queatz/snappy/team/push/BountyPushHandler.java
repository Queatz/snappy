package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.queatz.snappy.R;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.BountySpec;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 10/18/15.
 */
public class BountyPushHandler extends PushHandler {
    public BountyPushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<BountySpec> push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        switch (push.action) {
            case Config.PUSH_ACTION_BOUNTY_FINISHED:
                builder = team.push.newNotification()
                        .setContentTitle(team.context.getString(R.string.your_bounty_was_finished))
                        .setContentText(team.context.getString(R.string.by, push.body.people.firstName));

                resultIntent = new Intent(team.context, Person.class);
                Bundle extras = new Bundle();
                extras.putString("person", push.body.people.id);
                extras.putString("show", "messages");
                resultIntent.putExtras(extras);
                pendingIntent = team.push.newIntentWithStack(resultIntent);
                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_SOCIAL);
                }

                team.push.show("bounty/" + push.body.id + "/finished", builder.build());

                break;
        }

    }
}
