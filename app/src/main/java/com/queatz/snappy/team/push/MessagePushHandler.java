package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.queatz.snappy.R;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.MessageSpec;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 10/18/15.
 */
public class MessagePushHandler extends PushHandler {
    public MessagePushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<MessageSpec> push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        switch (push.action) {
            case Config.PUSH_ACTION_MESSAGE:
                if(("person/" + push.body.from.id + "/messages").equals(team.view.getTop()))
                    break;

                RealmResults<DynamicRealmObject> contacts = team.realm.where("Thing")
                        .equalTo("source.id", team.auth.getUser())
                        .equalTo("seen", false)
                        .findAllSorted("updated");

                int count = 1;
                String summary = "";

                if(contacts.size() > 1) {
                    for (int i = 0; i < contacts.size() && i < 3; i++) {
                        if(contacts.get(i).getObject(Thing.TARGET).getString(Thing.ID).equals(push.body.from.id))
                            continue;

                        count++;

                        if(!summary.isEmpty())
                            summary += ", ";

                        summary += contacts.get(i).getObject(Thing.TARGET).getString(Thing.FIRST_NAME);
                    }
                }

                String title;
                String message;

                if(count > 1) {
                    title = String.format(team.context.getString(R.string.new_messages), Integer.toString(count));
                    message = summary;
                } else {
                    title = push.body.from.firstName;
                    message = push.body.message;
                }

                builder = team.push.newNotification()
                        .setContentTitle(title)
                        .setContentText(message);

                if(count > 1) {
                    resultIntent = new Intent(team.context, Main.class);
                    Bundle extras = new Bundle();
                    extras.putString("show", "messages");
                    resultIntent.putExtras(extras);
                    pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(pendingIntent);
                }
                else {
                    resultIntent = new Intent(team.context, Person.class);
                    Bundle extras = new Bundle();
                    extras.putString("person", push.body.from.id);
                    extras.putString("show", "messages");
                    resultIntent.putExtras(extras);
                    pendingIntent = team.push.newIntentWithStack(resultIntent);

                    builder.setContentIntent(pendingIntent);
                }

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_MESSAGE);
                }

                team.push.show("messages", builder.build());

                break;
        }

        fetch(push);
    }

    private void fetch(PushSpec<MessageSpec> push) {
        switch (push.action) {
            case Config.PUSH_ACTION_MESSAGE:
                team.api.get(Config.PATH_EARTH + "/" + push.body.id, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        DynamicRealmObject m = team.things.put(response);

                        if(m != null)
                            team.local.updateContactsForMessage(m);
                    }

                    @Override
                    public void fail(String response) {

                    }
                });

                break;
        }
    }
}
