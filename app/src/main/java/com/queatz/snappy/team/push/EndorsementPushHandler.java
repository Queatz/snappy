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
import com.queatz.snappy.shared.things.EndorsementSpec;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 12/26/15.
 */
public class EndorsementPushHandler extends PushHandler {
    public EndorsementPushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<EndorsementSpec> push) {
        NotificationCompat.Builder builder;
        Intent resultIntent;
        Bundle extras;

        switch (push.action) {
            case Config.PUSH_ACTION_NEW_OFFER:
                builder = team.push.newNotification()
                        .setContentTitle(team.context.getString(R.string.person_endorsed_you, push.body.source.firstName))
                        .setContentText(push.body.target.details);

                resultIntent = new Intent(team.context, Person.class);
                extras = new Bundle();
                extras.putString("person", team.auth.getUser());
                resultIntent.putExtras(extras);
                builder.setContentIntent(team.push.newIntentWithStack(resultIntent));

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_SOCIAL);
                }

                team.push.show("endorsement/" + push.body.id + "/new", builder.build());

                break;
        }
    }
}
