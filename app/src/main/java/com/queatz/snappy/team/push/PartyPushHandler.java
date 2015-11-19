package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.queatz.snappy.Background;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.util.TimeUtil;

/**
 * Created by jacob on 10/18/15.
 */
public class PartyPushHandler extends PushHandler {
    public PartyPushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<PartySpec> push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        switch (push.action) {
            case Config.PUSH_ACTION_JOIN_PARTY:
                // Do: notify followers that you are joining a party (once accepted by the host)
                // Don't: notify the host that you are going to their party if you are their follower

                break;
            case Config.PUSH_ACTION_NEW_PARTY:
                builder = team.push.newNotification()
                        .setContentTitle(String.format(team.context.getString(R.string.party_by_person), push.body.name, push.body.host.firstName))
                        .setContentText(String.format(team.context.getString(R.string.party_starts_at), TimeUtil.cuteDate(push.body.date, true)))
                        .setPriority(Notification.PRIORITY_LOW)
                        .setDefaults(Notification.DEFAULT_LIGHTS);

                resultIntent = new Intent(team.context, Main.class);
                pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 20) {
                    resultIntent = new Intent(team.context, Background.class);
                    Bundle extras = new Bundle();
                    extras.putString(Config.EXTRA_ACTION, Config.EXTRA_ACTION_JOIN_REQUEST);
                    extras.putString(Config.EXTRA_PARTY_ID, push.body.id);
                    resultIntent.putExtras(extras);

                    pendingIntent = PendingIntent.getService(team.context, 0, resultIntent, 0);

                    builder.addAction(new NotificationCompat.Action(0, team.context.getString(R.string.interested), pendingIntent));
                }

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_EVENT);
                }

                team.push.show("party/" + push.body.id, builder.build());

                break;
        }
    }
}