package com.queatz.snappy.team;

import android.location.Location;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;

import io.realm.DynamicRealmObject;

import static com.queatz.snappy.team.EarthGraphQuery.*;

public class Earth {

    private final Team team;

    public Earth(Team team) {
        this.team = team;
    }

    public void acceptJoin(DynamicRealmObject join, Api.Callback callback) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_ACCEPT, true);
        team.api.post(Config.PATH_EARTH + "/" + join.getString(Thing.ID), params, callback);
    }

    public void getPersonById(String personId, Api.Callback callback) {
        team.api.get(Config.PATH_EARTH + "/" + personId, SELECT_PERSON.params(), callback);
    }


    public void me(String email, String googleAuthToken, Api.Callback callback) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_EMAIL, email);
        team.api.get(Config.PATH_EARTH + "/" + Config.PATH_ME, SELECT_ME.appendTo(params), callback);
    }

    public void me(Api.Callback callback) {
        team.api.get(Config.PATH_EARTH + "/" + Config.PATH_ME, SELECT_ME.params(), callback);
    }

    public void thing(String thingId, Api.Callback callback) {
        team.api.get(Config.PATH_EARTH + "/" + thingId, callback);
    }

    public void thingLikers(String thingId, Api.Callback callback) {
        team.api.get(Config.PATH_EARTH + "/" + thingId + "/" + Config.PATH_LIKERS, callback);
    }

    public void nearHere(String kind, Location location, Api.Callback callback) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LATITUDE, location.getLatitude());
        params.put(Config.PARAM_LONGITUDE, location.getLongitude());
        params.put(Config.PARAM_RECENT, true);
        team.api.get(Config.PATH_EARTH + "/" + Config.PATH_HERE + "/" + kind, SELECT_HOME.appendTo(params), callback);
    }

    public void meMessages(Api.Callback callback) {
        team.api.get(Config.PATH_EARTH + "/" + Config.PATH_ME + "/" + Config.PATH_MESSAGES, SELECT_MESSAGES.params(), callback);
    }

    public void thingFollowers(String thingId, Api.Callback callback) {
        team.api.get(Config.PATH_EARTH + "/" + String.format(Config.PATH_PEOPLE_FOLLOWERS, thingId), SELECT_THINGS.params(), callback);
    }

    public void thingFollowing(String thingId, Api.Callback callback) {
        team.api.get(Config.PATH_EARTH + "/" + String.format(Config.PATH_PEOPLE_FOLLOWERS, thingId), SELECT_THINGS.params(), callback);
    }
}
