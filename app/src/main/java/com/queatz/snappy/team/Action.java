package com.queatz.snappy.team;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.PersonList;
import com.queatz.snappy.things.Contact;
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Message;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.ui.MiniMenu;
import com.queatz.snappy.ui.TextView;

import java.io.FileNotFoundException;
import java.util.Date;

import io.realm.RealmResults;

/**
 * Created by jacob on 11/23/14.
 */

public class Action {
    public Team team;

    public Action(Team t) {
        team = t;
    }

    public void setSeen(@NonNull final Person person) {
        RealmResults<Contact> contacts = team.realm.where(Contact.class)
                .equalTo("person.id", team.auth.getUser())
                .equalTo("contact.id", person.getId())
                .findAll();

        boolean changed = false;

        team.realm.beginTransaction();

        for(int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);

            if(!contact.isSeen()) {
                contact.setSeen(true);
                changed = true;
            }
        }

        if(changed) {
            team.realm.commitTransaction();

            RequestParams params = new RequestParams();
            params.put(Config.PARAM_SEEN, true);

            team.api.post(String.format(Config.PATH_PEOPLE_ID, person.getId()), params);
        }
        else {
            team.realm.cancelTransaction();
        }
    }

    public void openMessages(Activity from, @NonNull final Person person) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getId());
        bundle.putString("show", "messages");

        team.view.show(from, com.queatz.snappy.activity.Person.class, bundle);
    }

    public void sendMessage(@NonNull final Person to, @NonNull final String message) {
        final String localId = Util.createLocalId();

        team.realm.beginTransaction();
        Message o = team.realm.createObject(Message.class);
        o.setId(localId);
        o.setFrom(team.auth.me());
        o.setTo(to);
        o.setMessage(message);
        o.setDate(new Date());

        team.realm.commitTransaction();

        team.local.updateContactsForMessage(o);

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);
        params.put(Config.PARAM_MESSAGE, message);

        team.api.post(String.format(Config.PATH_PEOPLE_ID, to.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Message.class, response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Message not sent", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showFollowers(Activity from, @NonNull final Person person) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getId());
        bundle.putBoolean("showFollowing", false);

        team.view.show(from, PersonList.class, bundle);
    }

    public void showFollowing(Activity from, @NonNull final Person person) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getId());
        bundle.putBoolean("showFollowing", true);

        team.view.show(from, PersonList.class, bundle);
    }

    public void followPerson(@NonNull final Person person) {
        final String localId = Util.createLocalId();

        team.realm.beginTransaction();
        Follow o = team.realm.createObject(Follow.class);
        o.setId(localId);
        o.setPerson(team.auth.me());
        o.setFollowing(person);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);
        params.put(Config.PARAM_FOLLOW, true);

        team.api.post(String.format(Config.PATH_PEOPLE_ID, person.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Follow.class, response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Follow failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openDate(Activity from, @NonNull final Party party) {
        if(party == null)
            return;

        Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
        intent.setType("vnd.android.cursor.dir/event");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, party.getDate().getTime());
        intent.putExtra(CalendarContract.Events.TITLE, party.getName());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, party.getDetails());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, party.getLocation().getText());

        from.startActivity(intent);
    }

    public void openLocation(Activity from, com.queatz.snappy.things.Location location) {
        if(location == null)
            return;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + location.getText()));

        from.startActivity(intent);
    }

    public void openProfile(Activity from, @NonNull final Person person) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getId());
        team.view.show(from, com.queatz.snappy.activity.Person.class, bundle);
    }

    public void openMinimenu(Activity in, View source) {
        TextView host = (TextView) in.findViewById(R.id.miniMenu).findViewById(R.id.action_host);

        if (host != null) {
            if(team.buy.bought()) {
                host.setText(R.string.host_a_party);
            }
        }

        ((MiniMenu) in.findViewById(R.id.miniMenu)).show();
    }

    public void markPartyFull(@NonNull final Party party) {
        team.realm.beginTransaction();
        party.setFull(true);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FULL, true);

        team.api.post(String.format(Config.PATH_PARTY_ID, party.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Mark full failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void joinParty(final Activity activity, @NonNull final Party party) {
        final String localId = Util.createLocalId();

        team.realm.beginTransaction();
        Join o = team.realm.createObject(Join.class);
        o.setId(localId);
        o.setPerson(team.auth.me());
        o.setParty(party);
        o.setStatus(Config.JOIN_STATUS_REQUESTED);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);
        params.put(Config.PARAM_JOIN, true);

        team.api.post(String.format(Config.PATH_PARTY_ID, party.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Join.class, response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Join failed", Toast.LENGTH_SHORT).show();
            }
        });

        new AlertDialog.Builder(activity)
                .setMessage(String.format(team.context.getString(R.string.message_join_party), party.getHost().getFirstName()))
                .setPositiveButton(team.context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openMessages(activity, party.getHost());
                    }
                })
                .setNegativeButton(team.context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public void cancelJoin(@NonNull final Party party) {
        RealmResults<Join> joins = team.realm.where(Join.class)
                .equalTo("person.id", team.auth.getUser())
                .equalTo("party.id", party.getId())
                .findAll();

        team.realm.beginTransaction();

        for(int i = 0; i < joins.size(); i++) {
            Join join = joins.get(i);
            join.setStatus(Config.JOIN_STATUS_WITHDRAWN);
        }

        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_CANCEL_JOIN, true);

        team.api.post(String.format(Config.PATH_PARTY_ID, party.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Join cancel failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void acceptJoin(@NonNull final Join join) {
        team.realm.beginTransaction();
        join.setStatus(Config.JOIN_STATUS_IN);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_ACCEPT, true);

        team.api.post(String.format(Config.PATH_JOIN_ID, join.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Accept failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void hideJoin(@NonNull final Join join) {
        team.realm.beginTransaction();
        join.setStatus(Config.JOIN_STATUS_OUT);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_HIDE, true);

        team.api.post(String.format(Config.PATH_JOIN_ID, join.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Hide join failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void hostParty(final Activity activity, final String group, final String name, final Date date, final com.queatz.snappy.things.Location location, final String details) {
        RequestParams params = new RequestParams();

        if(group != null && !group.isEmpty())
            params.put("id", group);

        params.put("name", name);
        params.put("date", Util.dateToString(date));
        params.put("location", location.getId() == null ? location.getJson() : location.getId());
        params.put("details", details);

        team.api.post(Config.PATH_PARTIES, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Party.class, response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Host party failed", Toast.LENGTH_SHORT).show();
            }
        });

        showPartiesPostHost(activity);
    }

    public void showPartiesPostHost(final Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("show_post_host_message", true);

        team.view.show(activity, Main.class, bundle);
    }

    public boolean uploadUpto(Uri image, String location) { // TODO this will turn into post photo to party
        RequestParams params = new RequestParams();

        try {
            params.put("photo", team.context.getContentResolver().openInputStream(image));
            params.put("location", location);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        team.api.post(Config.PATH_ME_UPTO, params, new Api.Callback() {
            @Override
            public void success(String response) {
                Log.d(Config.LOG_TAG, "yay new upto posted");
            }

            @Override
            public void fail(String response) {
                Log.e(Config.LOG_TAG, "error uploading new upto: " + response);
            }
        });

        return true;
    }
}
