package com.queatz.snappy.team.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.queatz.snappy.R;
import com.queatz.snappy.activity.Quests;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.QuestSpec;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Quest;

import java.util.ArrayList;

/**
 * Created by jacob on 10/18/15.
 *
 * @deprecated See {@code OfferPushHandler}
 */
public class QuestPushHandler extends PushHandler {
    public QuestPushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<QuestSpec> push) {
        NotificationCompat.Builder builder;
        PendingIntent pendingIntent;
        Intent resultIntent;

        switch (push.action) {
            case Config.PUSH_ACTION_QUEST_STARTED:
                String str;

                switch (push.body.team.size()) {
                    case 1:
                        str = team.context.getString(R.string.person_started_quest, push.body.team.get(0).firstName, push.body.name);
                        break;
                    default:
                        str = team.context.getString(R.string.quest_has_a_team, push.body.name);
                        break;
                }

                builder = team.push.newNotification()
                        .setContentTitle(team.context.getString(R.string.quest_started))
                        .setContentText(str);

                resultIntent = new Intent(team.context, Quests.class);
                pendingIntent = team.push.newIntentWithStack(resultIntent);

                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_SOCIAL);
                }

                team.push.show("quest/" + push.body.id + "/started", builder.build());

                break;
            case Config.PUSH_ACTION_QUEST_COMPLETED:
                int other;

                switch (push.body.team.size()) {
                    case 1:
                        str = team.context.getResources().getString(R.string.you_completed_quest, push.body.name);
                        break;
                    case 2:
                        other = team.auth.me().getFirstName().equals(push.body.team.get(0).firstName) ? 1 : 0;
                        str = team.context.getResources().getString(R.string.you_completed_quest_with_person, push.body.name, push.body.team.get(other).firstName);
                        break;
                    case 3:
                        boolean accountedForMe = false;
                        ArrayList<String> otherNames = new ArrayList<>();

                        for (int i = 0; i < push.body.team.size(); i++) {
                            String otherName = push.body.team.get(i).firstName;

                            if (accountedForMe || !team.auth.me().getFirstName().equals(otherName)) {
                                otherNames.add(otherName);
                            } else {
                                accountedForMe = true;
                            }
                        }

                        str = team.context.getResources().getString(R.string.you_completed_quest_with_two_people, push.body.name, otherNames.get(0), otherNames.get(1));
                        break;
                    default:
                        str = team.context.getResources().getString(R.string.you_completed_quest_with_more_people, push.body.name);
                        break;
                }

                builder = team.push.newNotification()
                        .setContentTitle(team.context.getString(R.string.quest_completed))
                        .setContentText(str);

                resultIntent = new Intent(team.context, Quests.class);
                pendingIntent = team.push.newIntentWithStack(resultIntent);

                builder.setContentIntent(pendingIntent);

                if(Build.VERSION.SDK_INT >= 21) {
                    builder
                            .setColor(team.context.getResources().getColor(R.color.red))
                            .setCategory(Notification.CATEGORY_SOCIAL);
                }

                team.push.show("quest/" + push.body.id + "/completed", builder.build());

                break;
        }
    }

    private void fetch(PushSpec<QuestSpec> push) {
        switch (push.action) {
            case Config.PUSH_ACTION_QUEST_STARTED:
            case Config.PUSH_ACTION_QUEST_COMPLETED:
                team.api.get(String.format(Config.PATH_QUEST_ID, push.body.id), new Api.Callback() {
                    @Override
                    public void success(String response) {
                        team.things.put(Quest.class, response);
                    }

                    @Override
                    public void fail(String response) {

                    }
                });

        }
    }
}
