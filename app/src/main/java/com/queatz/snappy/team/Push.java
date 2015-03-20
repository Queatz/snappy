package com.queatz.snappy.team;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.queatz.snappy.Config;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.things.Contact;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Message;

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
        mNotificationManager.cancel(push, 0);
    }

    public void got(JSONObject push) {
        try {
            String personFirstName;
            String personId;
            String partyName;
            Notification.Builder builder;

            switch (push.getString("action")) {
                case Config.PUSH_ACTION_MESSAGE:
                    personId = push.getJSONObject("message").getJSONObject("from").getString("id");

                    if(("person/" + personId + "/messages").equals(team.view.getTop()))
                        break;

                    personFirstName = URLDecoder.decode(push.getJSONObject("message").getJSONObject("from").getString("firstName"), "UTF-8");
                    String message = URLDecoder.decode(push.getJSONObject("message").getString("message"), "UTF-8");
                    String messageId = URLDecoder.decode(push.getJSONObject("message").getString("id"), "UTF-8");

                    RealmResults<Contact> contacts = team.realm.where(Contact.class).equalTo("seen", false).findAllSorted("updated");

                    int count = 1;
                    String summary = "";

                    if(contacts.size() > 1) {
                        for (int i = 0; i < contacts.size() && i < 3; i++) {
                            if(contacts.get(i).getLast().getId().equals(messageId))
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

                    builder = new Notification.Builder(team.context)
                            .setAutoCancel(true)
                            .setContentTitle(personFirstName)
                            .setContentText(message)
                            .setSmallIcon(R.drawable.rocket)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setDefaults(Notification.DEFAULT_ALL);

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_MESSAGE);
                    }

                    show("messages", builder.build());

                    break;
                case Config.PUSH_ACTION_JOIN_REQUEST:
                    personFirstName = URLDecoder.decode(push.getJSONObject("person").getString("firstName"), "UTF-8");
                    personId = URLDecoder.decode(push.getJSONObject("person").getString("id"), "UTF-8");
                    partyName = URLDecoder.decode(push.getJSONObject("party").getString("name"), "UTF-8");

                    builder = new Notification.Builder(team.context)
                            .setAutoCancel(true)
                            .setContentTitle(personFirstName)
                            .setContentText(String.format(team.context.getString(R.string.requested_to_join_party), partyName))
                            .setSmallIcon(R.drawable.rocket)
                            .setPriority(Notification.PRIORITY_DEFAULT)
                            .setDefaults(Notification.DEFAULT_ALL);

                    if(Build.VERSION.SDK_INT >= 20) {
                        builder.addAction(new Notification.Action(0, "Accept", null));
                    }

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_EVENT);
                    }

                    show("join/" + personId, builder.build());

                    break;
                case Config.PUSH_ACTION_JOIN_ACCEPTED:
                    partyName = URLDecoder.decode(push.getJSONObject("party").getString("name"), "UTF-8");
                    String partyId = URLDecoder.decode(push.getJSONObject("party").getString("id"), "UTF-8");
                    Date partyDate = Util.stringToDate(push.getJSONObject("party").getString("date"));

                    builder = new Notification.Builder(team.context)
                            .setAutoCancel(true)
                            .setContentTitle(partyName)
                            .setContentText(String.format(team.context.getString(R.string.request_accepted), Util.relDate(partyDate)))
                            .setSmallIcon(R.drawable.rocket)
                            .setPriority(Notification.PRIORITY_DEFAULT)
                            .setDefaults(Notification.DEFAULT_ALL);

                    if(Build.VERSION.SDK_INT >= 20) {
                        builder.addAction(new Notification.Action(0, "Accept", null));
                    }

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_EVENT);
                    }

                    show("accept/" + partyId, builder.build());

                    break;
                case Config.PUSH_ACTION_FOLLOW:
                    personFirstName = URLDecoder.decode(push.getJSONObject("person").getString("firstName"), "UTF-8");

                    builder = new Notification.Builder(team.context)
                            .setAutoCancel(true)
                            .setContentTitle(personFirstName)
                            .setContentText(team.context.getString(R.string.started_following_you))
                            .setSmallIcon(R.drawable.rocket)
                            .setPriority(Notification.PRIORITY_DEFAULT)
                            .setDefaults(Notification.DEFAULT_ALL);

                    if(Build.VERSION.SDK_INT >= 21) {
                        builder
                                .setColor(team.context.getResources().getColor(R.color.red))
                                .setCategory(Notification.CATEGORY_SOCIAL);
                    }

                    show("follow", builder.build());

                    break;
            }

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
            }
        }
        catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }
}
