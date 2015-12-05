package com.queatz.snappy.team;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.JsonObject;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.BountySpec;
import com.queatz.snappy.shared.things.FollowLinkSpec;
import com.queatz.snappy.shared.things.JoinLinkSpec;
import com.queatz.snappy.shared.things.MessageSpec;
import com.queatz.snappy.shared.things.OfferSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.QuestSpec;
import com.queatz.snappy.shared.things.UpdateSpec;
import com.queatz.snappy.team.push.BountyPushHandler;
import com.queatz.snappy.team.push.DefaultPushHandler;
import com.queatz.snappy.team.push.FollowPushHandler;
import com.queatz.snappy.team.push.JoinPushHandler;
import com.queatz.snappy.team.push.MessagePushHandler;
import com.queatz.snappy.team.push.OfferPushHandler;
import com.queatz.snappy.team.push.PartyPushHandler;
import com.queatz.snappy.team.push.QuestPushHandler;
import com.queatz.snappy.team.push.UpdatePushHandler;
import com.queatz.snappy.util.Json;

/**
 * Created by jacob on 3/19/15.
 */
public class Push {
    public Team team;
    private NotificationManager mNotificationManager;

    private BountyPushHandler bountyPushHandler;
    private DefaultPushHandler defaultPushHandler;
    private QuestPushHandler questPushHandler;
    private FollowPushHandler followPushHandler;
    private JoinPushHandler joinPushHandler;
    private MessagePushHandler messagePushHandler;
    private PartyPushHandler partyPushHandler;
    private UpdatePushHandler updatePushHandler;
    private OfferPushHandler offerPushHandler;

    public Push(Team t) {
        team = t;
        mNotificationManager = (NotificationManager) team.context.getSystemService(Context.NOTIFICATION_SERVICE);

        bountyPushHandler = new BountyPushHandler(team);
        defaultPushHandler = new DefaultPushHandler(team);
        questPushHandler = new QuestPushHandler(team);
        followPushHandler = new FollowPushHandler(team);
        joinPushHandler = new JoinPushHandler(team);
        messagePushHandler = new MessagePushHandler(team);
        partyPushHandler = new PartyPushHandler(team);
        updatePushHandler = new UpdatePushHandler(team);
        offerPushHandler = new OfferPushHandler(team);
    }

    public void show(String push, Notification notification) {
        mNotificationManager.notify(push, 0, notification);
    }

    public void clear(String push) {
        clear(push, true);
    }

    public void clear(String push, boolean clearEverywhere) {
        mNotificationManager.cancel(push, 0);

        if(clearEverywhere) {
            RequestParams params = new RequestParams();
            params.put("notification", push);
            team.api.post(Config.PATH_ME_CLEAR_NOTIFICATION, params);
        }
    }

    private <T> PushSpec<T> gen(String action, JsonObject json, Class<T> clazz) {
        return new PushSpec<>(action, Json.from(json.get("body").getAsJsonObject(), clazz));
    }

    public void got(String message) {
        JsonObject json;

        try {
            json = Json.from(message, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (!json.has("action")) {
            Log.w(Config.LOG_TAG, "Push got with no action");
            return;
        }

        String action = json.get("action").getAsString();
        switch (action) {
            case Config.PUSH_ACTION_MESSAGE:
                messagePushHandler.got(gen(action, json, MessageSpec.class));
                break;
            case Config.PUSH_ACTION_FOLLOW:
                followPushHandler.got(gen(action, json, FollowLinkSpec.class));
                break;
            case Config.PUSH_ACTION_REFRESH_ME:
            case Config.PUSH_ACTION_CLEAR_NOTIFICATION:
                defaultPushHandler.got(new PushSpec<>(action, json.get("body")));
                break;
            case Config.PUSH_ACTION_NEW_PARTY:
                partyPushHandler.got(gen(action, json, PartySpec.class));
                break;
            case Config.PUSH_ACTION_JOIN_PARTY:
            case Config.PUSH_ACTION_JOIN_REQUEST:
            case Config.PUSH_ACTION_JOIN_ACCEPTED:
                joinPushHandler.got(gen(action, json, JoinLinkSpec.class));
                break;
            case Config.PUSH_ACTION_BOUNTY_FINISHED:
                bountyPushHandler.got(gen(action, json, BountySpec.class));
                break;
            case Config.PUSH_ACTION_QUEST_COMPLETED:
            case Config.PUSH_ACTION_QUEST_STARTED:
                questPushHandler.got(gen(action, json, QuestSpec.class));
                break;
            case Config.PUSH_ACTION_NEW_UPTO:
                updatePushHandler.got(gen(action, json, UpdateSpec.class));
                break;
            case Config.PUSH_ACTION_NEW_OFFER:
                offerPushHandler.got(gen(action, json, OfferSpec.class));
                break;
            case Config.PUSH_ACTION_HOSTING_REMINDER:
            default:
                Log.w(Config.LOG_TAG, "Push received with no action: " + action);
                break;
        }
    }

    public PendingIntent newIntentWithStack(Intent resultIntent) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(team.context);
        stackBuilder.addParentStack(Main.class);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public NotificationCompat.Builder newNotification() {
        final Uri sound = Uri.parse("android.resource://" + team.context.getPackageName() + "/" + R.raw.completetask);

        return new NotificationCompat.Builder(team.context)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.icon_system)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSound(sound, AudioManager.STREAM_NOTIFICATION);
    }
}
