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
import com.queatz.snappy.shared.things.UpdateSpec;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 10/18/15.
 */
public class UpdatePushHandler extends PushHandler {
    public UpdatePushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<UpdateSpec> push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        switch (push.action) {
            case Config.PUSH_ACTION_NEW_UPTO:
                builder = team.push.newNotification()
                        .setContentTitle(push.body.person.firstName)
                        .setContentText(team.context.getString(R.string.added_a_new_photo));

                resultIntent = new Intent(team.context, Person.class);
                Bundle extras = new Bundle();
                extras.putString("person", push.body.person.id);
                resultIntent.putExtras(extras);
                builder.setContentIntent(team.push.newIntentWithStack(resultIntent));

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_SOCIAL);
                }

                team.push.show("person/" + push.body.person.id + "/upto", builder.build());

                break;
        }
    }
}
