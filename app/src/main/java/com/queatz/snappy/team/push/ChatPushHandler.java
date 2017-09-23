package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 9/18/17.
 */

public class ChatPushHandler extends PushHandler {
    public ChatPushHandler(Team team) {
        super(team);
    }

    public void got(JsonObject push) {
        NotificationCompat.Builder builder;
        Intent resultIntent;

        if(("main.chat").equals(team.view.getTop())) {
            return;
        }

        String topic = push.getAsJsonObject("topic").getAsString();

        String message = team.context.getString(R.string.new_chat_activity);

        builder = team.push.newNotification()
                .setVibrate(null)
                .setSound(null)
                .setContentTitle(topic)
                .setContentText(message);

        Bundle extras = new Bundle();
        extras.putString(Config.EXTRA_SHOW, "chat");
        resultIntent = new Intent(team.context, Main.class);
        resultIntent.putExtras(extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= 21) {
            builder
                    .setColor(team.context.getResources().getColor(R.color.red))
                    .setCategory(Notification.CATEGORY_SOCIAL);
        }

        team.push.show("chat", builder.build());
    }
}
