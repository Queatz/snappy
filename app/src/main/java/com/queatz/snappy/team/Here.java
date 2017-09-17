package com.queatz.snappy.team;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;

import com.loopj.android.http.RequestParams;
import com.queatz.branch.Branch;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.actions.UpdateThings;
import com.queatz.snappy.team.contexts.ActivityContext;

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

    public void getRecentUpdates(final Activity activity) {
        team.location.get(activity, new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
            @Override
            public void onLocationFound(final android.location.Location location) {
                final RequestParams params = new RequestParams();
                params.put("latitude", location.getLatitude());
                params.put("longitude", location.getLongitude());

                team.api.get(Config.PATH_EARTH + "/" + Config.PATH_HERE + "/update?recent=true", params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        Branch.from((ActivityContext) activity).to(new UpdateThings(response));
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

                team.api.get(Config.PATH_EARTH + "/" + Config.PATH_HERE + "/person|resource|project|offer|club|hub|form|party", params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        if(refresher != null) {
                            refresher.setRefreshing(false);
                        }

                        Branch.from((ActivityContext) activity).to(new UpdateThings(response).when(RealmResults.class, new Branch<RealmResults>() {
                            @Override
                            protected void execute() {
                                if(callback != null) {
                                    RealmList <DynamicRealmObject> results = new RealmList<>();
                                    results.addAll(((RealmResults<DynamicRealmObject>) me()).subList(0, me().size()));
                                    callback.onSuccess(results);
                                }
                            }
                        }));
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
