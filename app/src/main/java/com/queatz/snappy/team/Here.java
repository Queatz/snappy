package com.queatz.snappy.team;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.things.Bounty;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Quest;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;

/**
 * Created by jacob on 9/5/15.
 */
public class Here {
    public Team team;

    public Here(Team t) {
        team = t;
    }

    public interface Callback {
        void onSuccess(RealmList<Person> people, RealmList<com.queatz.snappy.things.Location> locations, RealmList<Party> parties, RealmList<Bounty> bounties, RealmList<Quest> quests);
    }

    public void update(final Activity activity, final SwipeRefreshLayout refresher, final Callback callback) {
        team.location.get(activity, new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
            @Override
            public void onLocationFound(final android.location.Location location) {
                final RequestParams params = new RequestParams();
                params.put("latitude", location.getLatitude());
                params.put("longitude", location.getLongitude());

                team.api.get(Config.PATH_HERE, params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        if(refresher != null) {
                            refresher.setRefreshing(false);
                        }

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            RealmList<com.queatz.snappy.things.Location> locations = null;
                            RealmList<Person> people = null;
                            RealmList<Bounty> bounties = null;
                            RealmList<Party> parties = null;
                            RealmList<Quest> quests = null;

                            if(jsonObject.has("locations")) {
                                locations = team.things.putAll(com.queatz.snappy.things.Location.class, jsonObject.getJSONArray("locations"));
                            }

                            if(jsonObject.has("people")) {
                                people = team.things.putAll(Person.class, jsonObject.getJSONArray("people"));
                            }

                            if(jsonObject.has("parties")) {
                                parties = team.things.putAll(Party.class, jsonObject.getJSONArray("parties"));
                            }

                            if(jsonObject.has("bounties")) {
                                //TODO temp for delete arch (send my id list, server says which are gone)
                                team.realm.beginTransaction();
                                team.realm.clear(Bounty.class);
                                team.realm.commitTransaction();

                                bounties = team.things.putAll(Bounty.class, jsonObject.getJSONArray("bounties"));
                            }

                            if(jsonObject.has("quests")) {
                                //TODO temp for delete arch (send my id list, server says which are gone)
                                team.realm.beginTransaction();
                                team.realm.clear(Quest.class);
                                team.realm.commitTransaction();

                                quests = team.things.putAll(Quest.class, jsonObject.getJSONArray("quests"));
                            }

                            if(callback != null) {
                                callback.onSuccess(people, locations, parties, bounties, quests);
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void fail(String response) {
                        if(refresher != null) {
                            refresher.setRefreshing(false);
                        }
                    }
                });
            }

            @Override
            public void onLocationUnavailable() {
                if(refresher != null) {
                    refresher.setRefreshing(false);
                }
            }
        });
    }
}
