package com.queatz.snappy.team;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Background;
import com.queatz.snappy.Config;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.activity.Quests;
import com.queatz.snappy.things.Contact;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Message;
import com.queatz.snappy.things.Quest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmResults;

/**
 * Created by jacob on 3/19/15.
 */
public class Push {
    public Team team;
    private NotificationManager mNotificationManager;

    public Push(Team t) {
        team = t;
        mNotificationManager = (NotificationManager) team.context.getSystemService(Context.NOTIFICATION_SERVICE);
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

    public void got(JSONObject push) {
        try {
            String personFirstName;
            String personId;
            String partyId;
            String joinId;
            String partyName;
            Date partyDate;
            NotificationCompat.Builder builder;

            Intent resultIntent;
            TaskStackBuilder stackBuilder;
            PendingIntent pendingIntent;
            Bundle extras;

            String action = push.getString("action");

            if(action == null) {
                Log.w(Config.LOG_TAG, "Push received with no action: " + push);
                return;
            }

            switch (action) {
                case Config.PUSH_ACTION_CLEAR_NOTIFICATION:
                    String n = push.getString("notification");
                    clear(n, false);

                    break;
                case Config.PUSH_ACTION_REFRESH_ME:
                    team.buy.pullPerson();

                    break;
                case Config.PUSH_ACTION_JOIN_PARTY:
                    // Do: notify followers that you are joining a party (once accepted by the host)
                    // Don't: notify the host that you are going to their party if you are their follower

                    break;
                case Config.PUSH_ACTION_NEW_PARTY:
                    personFirstName = URLDecoder.decode(push.getJSONObject("host").getString("firstName"), "UTF-8");
                    partyId = push.getJSONObject("party").getString("id");
                    partyName = URLDecoder.decode(push.getJSONObject("party").getString("name"), "UTF-8");
                    partyDate = Util.stringToDate(push.getJSONObject("party").getString("date"));

                    builder = newNotification()
                            .setContentTitle(String.format(team.context.getString(R.string.party_by_person), partyName, personFirstName))
                            .setContentText(String.format(team.context.getString(R.string.party_starts_at), Util.cuteDate(partyDate, true)))
                            .setPriority(Notification.PRIORITY_LOW)
                            .setDefaults(Notification.DEFAULT_LIGHTS);

                    resultIntent = new Intent(team.context, Main.class);
                    pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(pendingIntent);

                    if(Build.VERSION.SDK_INT >= 20) {
                        resultIntent = new Intent(team.context, Background.class);
                        extras = new Bundle();
                        extras.putString(Config.EXTRA_ACTION, Config.EXTRA_ACTION_JOIN_REQUEST);
                        extras.putString(Config.EXTRA_PARTY_ID, partyId);
                        resultIntent.putExtras(extras);

                        pendingIntent = PendingIntent.getService(team.context, 0, resultIntent, 0);

                        builder.addAction(new NotificationCompat.Action(0, team.context.getString(R.string.interested), pendingIntent));
                    }

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_EVENT);
                    }

                    show("party/" + partyId, builder.build());

                    break;
                case Config.PUSH_ACTION_MESSAGE:
                    personId = push.getJSONObject("message").getJSONObject("from").getString("id");

                    if(("person/" + personId + "/messages").equals(team.view.getTop()))
                        break;

                    personFirstName = URLDecoder.decode(push.getJSONObject("message").getJSONObject("from").getString("firstName"), "UTF-8");
                    String message = URLDecoder.decode(push.getJSONObject("message").getString("message"), "UTF-8");

                    RealmResults<Contact> contacts = team.realm.where(Contact.class).equalTo("seen", false).findAllSorted("updated");

                    int count = 1;
                    String summary = "";

                    if(contacts.size() > 1) {
                        for (int i = 0; i < contacts.size() && i < 3; i++) {
                            if(contacts.get(i).getContact().getId().equals(personId))
                                continue;

                            count++;

                            if(!summary.isEmpty())
                                summary += ", ";

                            summary += contacts.get(i).getContact().getFirstName();
                        }
                    }

                    if(count > 1) {
                        personFirstName = String.format(team.context.getString(R.string.new_messages), Integer.toString(count));
                        message = summary;
                    }

                    builder = newNotification()
                            .setContentTitle(personFirstName)
                            .setContentText(message);

                    if(count > 1) {
                        resultIntent = new Intent(team.context, Main.class);
                        extras = new Bundle();
                        extras.putString("show", "messages");
                        resultIntent.putExtras(extras);
                        pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        builder.setContentIntent(pendingIntent);
                    }
                    else {
                        resultIntent = new Intent(team.context, Person.class);
                        extras = new Bundle();
                        extras.putString("person", personId);
                        extras.putString("show", "messages");
                        resultIntent.putExtras(extras);
                        stackBuilder = TaskStackBuilder.create(team.context);
                        stackBuilder.addParentStack(Person.class);
                        stackBuilder.addNextIntent(resultIntent);
                        pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                        builder.setContentIntent(pendingIntent);
                    }

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_MESSAGE);
                    }

                    show("messages", builder.build());

                    break;
                case Config.PUSH_ACTION_JOIN_REQUEST:
                    personFirstName = URLDecoder.decode(push.getJSONObject("person").getString("firstName"), "UTF-8");
                    personId = push.getJSONObject("person").getString("id");
                    partyName = URLDecoder.decode(push.getJSONObject("party").getString("name"), "UTF-8");
                    joinId = push.getString("join");

                    builder = newNotification()
                            .setContentTitle(personFirstName)
                            .setContentText(String.format(team.context.getString(R.string.requested_to_join_party), partyName));

                    resultIntent = new Intent(team.context, Main.class);
                    pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(pendingIntent);

                    if(Build.VERSION.SDK_INT >= 20) {
                        resultIntent = new Intent(team.context, Background.class);
                        extras = new Bundle();
                        extras.putString(Config.EXTRA_ACTION, Config.EXTRA_ACTION_JOIN_ACCEPT);
                        extras.putString(Config.EXTRA_JOIN_ID, joinId);
                        resultIntent.putExtras(extras);

                        pendingIntent = PendingIntent.getService(team.context, 0, resultIntent, 0);

                        builder.addAction(new NotificationCompat.Action(0, team.context.getString(R.string.accept), pendingIntent));
                    }

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_EVENT);
                    }

                    show("join_request/" + joinId, builder.build());

                    break;
                case Config.PUSH_ACTION_JOIN_ACCEPTED:
                    partyName = URLDecoder.decode(push.getJSONObject("party").getString("name"), "UTF-8");
                    partyId = push.getJSONObject("party").getString("id");
                    partyDate = Util.stringToDate(push.getJSONObject("party").getString("date"));
                    joinId = push.getString("join");

                    builder = newNotification()
                            .setContentTitle(partyName)
                            .setContentText(String.format(team.context.getString(R.string.request_accepted), Util.relDate(partyDate)));

                    resultIntent = new Intent(team.context, Main.class);
                    pendingIntent = PendingIntent.getActivity(team.context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(pendingIntent);

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_EVENT);
                    }

                    show("join_accept/" + joinId, builder.build());

                    break;
                case Config.PUSH_ACTION_FOLLOW:
                    personFirstName = URLDecoder.decode(push.getJSONObject("person").getString("firstName"), "UTF-8");
                    personId = push.getJSONObject("person").getString("id");

                    builder = newNotification()
                            .setContentTitle(personFirstName)
                            .setContentText(team.context.getString(R.string.started_following_you));

                    resultIntent = new Intent(team.context, Person.class);
                    extras = new Bundle();
                    extras.putString("person", personId);
                    resultIntent.putExtras(extras);
                    stackBuilder = TaskStackBuilder.create(team.context);
                    stackBuilder.addParentStack(Main.class);
                    stackBuilder.addNextIntent(resultIntent);
                    pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(pendingIntent);

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_SOCIAL);
                    }

                    show("follow", builder.build());

                    break;

                case Config.PUSH_ACTION_BOUNTY_FINISHED:
                    personFirstName = URLDecoder.decode(push.getJSONObject("people").getString("firstName"), "UTF-8");
                    personId = push.getJSONObject("people").getString("id");
                    String bountyId = push.getString("bounty");

                    builder = new NotificationCompat.Builder(team.context)
                            .setContentTitle(team.context.getString(R.string.your_bounty_was_finished))
                            .setContentText(team.context.getString(R.string.by, personFirstName));

                    resultIntent = new Intent(team.context, Person.class);
                    extras = new Bundle();
                    extras.putString("person", personId);
                    extras.putString("show", "messages");
                    resultIntent.putExtras(extras);
                    stackBuilder = TaskStackBuilder.create(team.context);
                    stackBuilder.addParentStack(Main.class);
                    stackBuilder.addNextIntent(resultIntent);
                    pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_SOCIAL);
                    }

                    show("bounty/" + bountyId + "/finished", builder.build());

                    break;
                case Config.PUSH_ACTION_QUEST_STARTED:
                    String questName = URLDecoder.decode(push.getJSONObject("quest").getString("name"), "UTF-8");
                    String questId = push.getJSONObject("quest").getString("id");

                    JSONArray questTeam = push.getJSONObject("quest").getJSONArray("team");

                    String str;

                    switch (questTeam.length()) {
                        case 1:
                            str = team.context.getString(R.string.person_started_quest, URLDecoder.decode(questTeam.getJSONObject(0).getString("firstName"), "UTF-8"), questName);
                            break;
                        default:
                            str = team.context.getString(R.string.quest_has_a_team, questName);
                            break;
                    }

                    builder = new NotificationCompat.Builder(team.context)
                            .setContentTitle(team.context.getString(R.string.quest_started))
                            .setContentText(str);

                    resultIntent = new Intent(team.context, Quests.class);
                    stackBuilder = TaskStackBuilder.create(team.context);
                    stackBuilder.addParentStack(Main.class);
                    stackBuilder.addNextIntent(resultIntent);
                    pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(pendingIntent);

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_SOCIAL);
                    }

                    show("quest/" + questId + "/started", builder.build());

                    break;
                case Config.PUSH_ACTION_QUEST_COMPLETED:
                    questName = URLDecoder.decode(push.getJSONObject("quest").getString("name"), "UTF-8");
                    questId = push.getJSONObject("quest").getString("id");
                    questTeam = push.getJSONObject("quest").getJSONArray("team");

                    int other;

                    switch (questTeam.length()) {
                        case 1:
                            str = team.context.getResources().getString(R.string.you_completed_quest, questName);
                            break;
                        case 2:
                            other = team.auth.me().getFirstName().equals(URLDecoder.decode(questTeam.getJSONObject(0).getString("firstName"), "UTF-8")) ? 1 : 0;
                            str = team.context.getResources().getString(R.string.you_completed_quest_with_person, questName, URLDecoder.decode(questTeam.getJSONObject(other).getString("firstName"), "UTF-8"));
                            break;
                        case 3:
                            boolean accountedForMe = false;
                            ArrayList<String> otherNames = new ArrayList<>();

                            for (int i = 0; i < questTeam.length(); i++) {
                                String otherName = URLDecoder.decode(questTeam.getJSONObject(i).getString("firstName"), "UTF-8");

                                if (accountedForMe || !team.auth.me().getFirstName().equals(otherName)) {
                                    otherNames.add(otherName);
                                } else {
                                    accountedForMe = true;
                                }
                            }

                            str = team.context.getResources().getString(R.string.you_completed_quest_with_two_people, questName, otherNames.get(0), otherNames.get(1));
                            break;
                        default:
                            str = team.context.getResources().getString(R.string.you_completed_quest_with_more_people, questName);
                            break;
                    }

                    builder = new NotificationCompat.Builder(team.context)
                            .setContentTitle(team.context.getString(R.string.quest_completed))
                            .setContentText(str);

                    resultIntent = new Intent(team.context, Quests.class);
                    stackBuilder = TaskStackBuilder.create(team.context);
                    stackBuilder.addParentStack(Main.class);
                    stackBuilder.addNextIntent(resultIntent);
                    pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(pendingIntent);

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_SOCIAL);
                    }

                    show("quest/" + questId + "/completed", builder.build());

                    break;
            }

            // Grab more data from server

            switch (push.getString("action")) {
                case Config.PUSH_ACTION_MESSAGE:
                    team.api.get(String.format(Config.PATH_MESSAGES_ID, push.getJSONObject("message").getString("id")), new Api.Callback() {
                        @Override
                        public void success(String response) {
                            try {
                                JSONObject o = new JSONObject(response);

                                Message m = team.things.put(Message.class, o);

                                if(m != null)
                                    team.local.updateContactsForMessage(m);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void fail(String response) {

                        }
                    });

                    break;
                case Config.PUSH_ACTION_JOIN_REQUEST:
                    team.api.get(String.format(Config.PATH_JOIN_ID, push.getString("join")), new Api.Callback() {
                        @Override
                        public void success(String response) {
                            try {
                                JSONObject o = new JSONObject(response);

                                team.things.put(Join.class, o);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void fail(String response) {

                        }
                    });

                    break;
                case Config.PUSH_ACTION_JOIN_ACCEPTED:
                    team.api.get(String.format(Config.PATH_JOIN_ID, push.getString("join")), new Api.Callback() {
                        @Override
                        public void success(String response) {
                            try {
                                JSONObject o = new JSONObject(response);

                                team.things.put(Join.class, o);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void fail(String response) {

                        }
                    });

                    break;
                case Config.PUSH_ACTION_FOLLOW:

                    break;

                case Config.PUSH_ACTION_QUEST_STARTED:
                case Config.PUSH_ACTION_QUEST_COMPLETED:
                    team.api.get(String.format(Config.PATH_QUEST_ID, push.getJSONObject("quest").getString("id")), new Api.Callback() {
                        @Override
                        public void success(String response) {
                            team.things.put(Quest.class, response);
                        }

                        @Override
                        public void fail(String response) {

                        }
                    });

                    break;
            }
        }
        catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    private NotificationCompat.Builder newNotification() {
        final Uri sound = Uri.parse("android.resource://" + team.context.getPackageName() + "/" + R.raw.completetask);

        return new NotificationCompat.Builder(team.context)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.icon_system)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSound(sound, AudioManager.STREAM_NOTIFICATION);
    }
}
