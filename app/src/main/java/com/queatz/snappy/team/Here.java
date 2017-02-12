package com.queatz.snappy.team;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.util.Json;

import io.realm.DynamicRealmObject;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by jacob on 9/5/15.
 */
public class Here {
    public Team team;

    public Here(Team t) {
        team = t;
    }

    public interface Callback {
        void onSuccess(RealmList<DynamicRealmObject> things);
    }

    public void getRecentUpdates(Activity activity) {
        team.location.get(activity, new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
            @Override
            public void onLocationFound(final android.location.Location location) {
                final RequestParams params = new RequestParams();
                params.put("latitude", location.getLatitude());
                params.put("longitude", location.getLongitude());

                team.api.get(Config.PATH_EARTH + "/" + Config.PATH_HERE + "/update?recent=true", params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        team.things.putAll(response);
                    }

                    @Override
                    public void fail(String response) {
                    }
                });
            }

            @Override
            public void onLocationUnavailable() {
            }
        });
    }

    public void update(final Activity activity, final SwipeRefreshLayout refresher, final Callback callback) {
        team.location.get(activity, new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
            @Override
            public void onLocationFound(final android.location.Location location) {
                final RequestParams params = new RequestParams();
                params.put("latitude", location.getLatitude());
                params.put("longitude", location.getLongitude());

                team.api.get(Config.PATH_EARTH + "/" + Config.PATH_HERE + "/offer|hub|person|party", params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        if(refresher != null) {
                            refresher.setRefreshing(false);
                        }

                        JsonArray jsonArray = Json.from(response, JsonArray.class);

                        RealmResults<DynamicRealmObject> removeOffers =
                                team.realm.where("Thing")
                                        .equalTo(Thing.KIND, "offer")
                                        .notEqualTo("person.id", team.auth.getUser())
                                        .findAll();

                        team.realm.beginTransaction();
                        removeOffers.deleteAllFromRealm();
                        team.realm.commitTransaction();

                        RealmList<DynamicRealmObject> things = team.things.putAll(jsonArray);

                        if(callback != null) {
                            callback.onSuccess(things);
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
