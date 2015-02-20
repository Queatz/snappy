package com.queatz.snappy.team;

import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.fragment.PersonList;
import com.queatz.snappy.things.*;
import com.queatz.snappy.ui.MiniMenu;

import java.io.FileNotFoundException;

/**
 * Created by jacob on 11/23/14.
 */

public class Action {
    public Team team;

    public Action(Team t) {
        team = t;
    }

    public void showFollowers(@NonNull Person person) {
        PersonList list = new PersonList();
        list.setPerson(person);
        list.setShowFollowing(false);
        team.view.push(ViewActivity.Transition.EXAMINE, ViewActivity.Transition.INSTANT, list);
    }

    public void showFollowing(@NonNull Person person) {
        PersonList list = new PersonList();
        list.setPerson(person);
        list.setShowFollowing(true);
        team.view.push(ViewActivity.Transition.EXAMINE, ViewActivity.Transition.INSTANT, list);
    }

    public void followPerson(@NonNull Person person) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FOLLOW, true);

        team.api.post(String.format(Config.PATH_PEOPLE_ID, person.getId()), params);
    }

    public void openDate(Party party) {
        if(party == null)
            return;

        Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
        intent.setType("vnd.android.cursor.dir/event");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, party.getDate().getTime());
        intent.putExtra(CalendarContract.Events.TITLE, party.getName());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, party.getDetails());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, party.getLocation().getText());

        team.view.startActivity(intent);
    }

    public void openLocation(com.queatz.snappy.things.Location location) {
        if(location == null)
            return;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + location.getText()));

        team.view.startActivity(intent);
    }

    public void openProfile(Person person) {
        com.queatz.snappy.fragment.Person fragment = new com.queatz.snappy.fragment.Person();
        fragment.setPerson(person);
        team.view.push(ViewActivity.Transition.SEXY_PROFILE, ViewActivity.Transition.IN_THE_VOID, fragment);
    }

    public void openMinimenu(View source) {
        ((MiniMenu) team.view.findViewById(R.id.miniMenu)).show();
    }

    public void markPartyFull(@NonNull Party party) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FULL, true);

        team.api.post(String.format(Config.PATH_PARTY_ID, party.getId()), params);
    }

    public void joinParty(@NonNull Party party) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_JOIN, true);

        team.api.post(String.format(Config.PATH_PARTY_ID, party.getId()), params);
    }

    public void acceptJoin(@NonNull Join join) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_ACCEPT, true);

        team.api.post(String.format(Config.PATH_JOIN_ID, join.getId()), params);
    }

    public void hostParty(String group, String name, String date, String location, String details) {
        RequestParams params = new RequestParams();

        if(group != null && !group.isEmpty())
            params.put("id", group);

        params.put("name", name);
        params.put("date", date);
        params.put("location", location);
        params.put("details", details);

        team.api.post(Config.PATH_PARTIES, params);
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
