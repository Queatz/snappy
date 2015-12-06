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
import com.queatz.snappy.shared.things.UpdateLikeSpec;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 12/5/15.
 */
public class LikePushHandler extends PushHandler {
    public LikePushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<UpdateLikeSpec> push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        switch (push.action) {
            case Config.PUSH_ACTION_LIKE_UPDATE:
                builder = team.push.newNotification()
                        .setContentTitle(push.body.source.firstName)
                        .setContentText(team.context.getString(R.string.liked_your_photo));

                resultIntent = new Intent(team.context, Person.class);
                Bundle extras = new Bundle();
                extras.putString(Config.EXTRA_PERSON_ID, push.body.source.id);
                resultIntent.putExtras(extras);
                pendingIntent = team.push.newIntentWithStack(resultIntent);

                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_SOCIAL);
                }

                team.push.show("liker/" + push.body.source.id, builder.build());

                break;
        }
    }
}
