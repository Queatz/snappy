package com.queatz.snappy.team;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.PersonList;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;
import com.queatz.snappy.util.ResponseUtil;
import com.queatz.snappy.util.TimeUtil;

import java.io.FileNotFoundException;
import java.util.Date;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 11/23/14.
 */

@Deprecated
public class Action {
    public Team team;

    public Action(Team t) {
        team = t;
    }

    public void showFollowers(@NonNull Activity from, @NonNull final DynamicRealmObject person) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getString(Thing.ID));
        bundle.putBoolean("showFollowing", false);

        team.view.show(from, PersonList.class, bundle);
    }

    public void showFollowing(@NonNull Activity from, @NonNull final DynamicRealmObject person) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getString(Thing.ID));
        bundle.putBoolean("showFollowing", true);

        team.view.show(from, PersonList.class, bundle);
    }

    public void followPerson(@NonNull final DynamicRealmObject person) {
        final String localId = Util.createLocalId();

        team.realm.beginTransaction();
        DynamicRealmObject o = team.realm.createObject("Thing");
        o.setString(Thing.KIND, "follower");
        o.setString(Thing.ID, localId);
        o.setObject(Thing.SOURCE, team.auth.me());
        o.setObject(Thing.TARGET, person);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);
        params.put(Config.PARAM_FOLLOW, true);

        team.api.post(Config.PATH_EARTH + "/" + person.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Follow failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void stopFollowingPerson(@NonNull DynamicRealmObject person) {
        DynamicRealmObject follow = team.realm.where("Thing")
                .equalTo(Thing.KIND, "follower")
                .equalTo("source.id", team.auth.getUser())
                .equalTo("target.id", person.getString(Thing.ID))
                .findFirst();

        if(follow != null) {
            team.realm.beginTransaction();
            follow.deleteFromRealm();
            team.realm.commitTransaction();
        }

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FOLLOW, false);

        team.api.post(Config.PATH_EARTH + "/" + person.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Tollback local deletion
                Toast.makeText(team.context, "Stop following failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void markPartyFull(@NonNull final DynamicRealmObject party) {
        team.realm.beginTransaction();
        party.setBoolean(Thing.FULL, true);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FULL, true);

        team.api.post(Config.PATH_EARTH + "/" + party.getString(Thing.ID), params, new Api.Callback() {
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

    public void joinParty(final Activity activity, @NonNull final String partyId) {
        DynamicRealmObject party = team.realm.where("Thing").equalTo("id", partyId).findFirst();

        if(party == null) {
            team.realm.beginTransaction();
            party = team.realm.createObject("Thing");
            party.setString(Thing.KIND, "party");
            party.setString(Thing.ID, partyId);
            team.realm.commitTransaction();
        }

        joinParty(activity, party);
    }

    public void joinParty(final Activity activity, @NonNull final DynamicRealmObject party) {
        String localId = null;

        DynamicRealmObject o = team.realm.where("Thing")
                .equalTo("target.id", party.getString(Thing.ID))
                .equalTo("source.id", team.auth.getUser())
                .findFirst();

        if(o == null) {
            localId = Util.createLocalId();

            team.realm.beginTransaction();
            o = team.realm.createObject("Thing");
            o.setString(Thing.KIND, "join");
            o.setString(Thing.ID, localId);
            o.setObject(Thing.SOURCE, team.auth.me());
            o.setObject(Thing.TARGET, party);
            o.setString(Thing.STATUS, Config.JOIN_STATUS_REQUESTED);
            team.realm.commitTransaction();
        }

        RequestParams params = new RequestParams();

        if(localId != null)
            params.put(Config.PARAM_LOCAL_ID, localId);

        params.put(Config.PARAM_JOIN, true);

        team.api.post(Config.PATH_EARTH + "/" + party.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (response == null) {
                    return;
                }

                team.things.put(response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, R.string.join_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancelJoin(@NonNull final DynamicRealmObject party) {
        RealmResults<DynamicRealmObject> joins = team.realm.where("Thing")
                .equalTo("source.id", team.auth.getUser())
                .equalTo("target.id", party.getString(Thing.ID))
                .findAll();

        team.realm.beginTransaction();

        for(int i = 0; i < joins.size(); i++) {
            DynamicRealmObject join = joins.get(i);
            join.setString(Thing.STATUS, Config.JOIN_STATUS_WITHDRAWN);
        }

        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_CANCEL_JOIN, true);

        team.api.post(Config.PATH_EARTH + "/" + party.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, R.string.offer_cancel_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void acceptJoin(@NonNull final String joinId) {
        DynamicRealmObject join = team.realm.where("Thing").equalTo(Thing.ID, joinId).findFirst();

        if(join == null) {
            team.realm.beginTransaction();
            join = team.realm.createObject("Thing");
            join.setString(Thing.KIND, "join");
            join.setString(Thing.ID, joinId);
            join.setString(Thing.STATUS, Config.JOIN_STATUS_REQUESTED);
            team.realm.commitTransaction();
        }

        acceptJoin(join);
    }

    public void acceptJoin(@NonNull final DynamicRealmObject join) {
        team.realm.beginTransaction();
        join.setString(Thing.STATUS, Config.JOIN_STATUS_IN);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_ACCEPT, true);

        team.api.post(Config.PATH_EARTH + "/" + join.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.push.clear("join/" + join.getString(Thing.ID) + "/request");
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Accept failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void hideJoin(@NonNull final DynamicRealmObject join) {
        team.realm.beginTransaction();
        join.setString(Thing.STATUS, Config.JOIN_STATUS_OUT);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_HIDE, true);

        team.api.post(Config.PATH_EARTH + "/" + join.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (!ResponseUtil.isSuccess(response)) {
                    Toast.makeText(team.context, R.string.hide_join_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, R.string.hide_join_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void hostParty(@NonNull final Activity activity, final String group, final String name, final Date date, final DynamicRealmObject location, final String details) {
        RequestParams params = new RequestParams();

        if(group != null && !group.isEmpty())
            params.put("id", group);

        params.put(Config.PARAM_KIND, "party");
        params.put(Config.PARAM_NAME, name);
        params.put(Config.PARAM_DATE, TimeUtil.dateToString(date));
        params.put(Config.PARAM_LOCATION, location.getString(Thing.ID) == null ? Functions.getLocationJson(location) : location.getString(Thing.ID));
        params.put(Config.PARAM_DETAILS, details);

        team.api.post(Config.PATH_EARTH, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Host party failed", Toast.LENGTH_SHORT).show();
            }
        });

        showPartiesPostHost(activity);
    }

    public void showPartiesPostHost(@NonNull final Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("show_post_host_message", true);

        team.view.show(activity, Main.class, bundle);
    }

    DynamicRealmObject nPendingOfferPhotoChange;

    public void addPhotoToOffer(@NonNull Activity activity, @NonNull DynamicRealmObject offer) {
        nPendingOfferPhotoChange = offer;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, Config.REQUEST_CODE_CHOOSER);
    }

    public void removePhotoFromOffer(@NonNull DynamicRealmObject offer) {
        team.realm.beginTransaction();
        offer.setBoolean(Thing.PHOTO, false);
        team.realm.commitTransaction();

        team.api.post(Config.PATH_EARTH + "/" + offer.getString(Thing.ID) + "/" + Config.PATH_PHOTO + "/" + Config.PATH_DELETE);
    }

    public boolean addOffer(@NonNull String details, @Nullable Boolean want, @Nullable Integer price, @Nullable String unit) {
        details = details.trim();

        if(details.isEmpty()) {
            return false;
        }

        if (team.auth.me() == null) {
            return false;
        }

        team.realm.beginTransaction();
        DynamicRealmObject offer = team.realm.createObject("Thing");
        offer.setString(Thing.KIND, "offer");
        offer.setString(Thing.ID, Util.createLocalId());
        offer.setString(Thing.ABOUT, details);

        if (price != null) {
            offer.setInt(Thing.PRICE, price);
        }

        offer.setString(Thing.UNIT, unit);
        offer.setObject(Thing.SOURCE, team.auth.me());
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_KIND, "offer");
        params.put(Config.PARAM_LOCAL_ID, offer.getString(Thing.ID));
        params.put(Config.PARAM_DETAILS, details);
        params.put(Config.PARAM_PRICE, price);
        params.put(Config.PARAM_UNIT, unit);
        params.put(Config.PARAM_IN, team.auth.me().getString(Thing.ID));

        if (want != null) {
            params.put(Config.PARAM_WANT, want);
        }

        team.api.post(Config.PATH_EARTH, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, R.string.couldnt_add_offer, Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    public DynamicRealmObject nPendingLocationPhotoChange;

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case Config.REQUEST_CODE_CHOOSER:
                if(resultCode == Activity.RESULT_OK) {
                    final Uri photo = intent.getData();

                    if (photo == null) {
                        return;
                    }

                    if(nPendingLocationPhotoChange != null) {
                        uploadPhoto(String.format(Config.PATH_EARTH_PHOTO, nPendingLocationPhotoChange.getString(Thing.ID)), photo);
                    } else if(nPendingOfferPhotoChange != null) {
                        uploadPhoto(String.format(Config.PATH_EARTH_PHOTO, nPendingOfferPhotoChange.getString(Thing.ID)), photo);
                    }
                }

                break;
        }
    }

    private void uploadPhoto(String path, Uri photo) {
        RequestParams params = new RequestParams();

        try {
            params.put(Config.PARAM_PHOTO, team.context.getContentResolver().openInputStream(photo));
        }
        catch (FileNotFoundException e) {
            Toast.makeText(team.context, R.string.couldnt_set_photo, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        Toast.makeText(team.context, R.string.changing_photo, Toast.LENGTH_SHORT).show();

        final String id;

        if (nPendingLocationPhotoChange != null) {
            id = nPendingLocationPhotoChange.getString(Thing.ID);
        } else if (nPendingOfferPhotoChange != null) {
            id = nPendingOfferPhotoChange.getString(Thing.ID);
        } else {
            id = null;
        }

        team.api.post(path, params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (id != null) {
                    String photoUrl = Config.API_URL + String.format(Config.PATH_EARTH_PHOTO + "?s=64&auth=" + team.auth.getAuthParam(), id);
                    Images.with(team.context).invalidate(photoUrl);
                }
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, R.string.couldnt_set_photo, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean want(@NonNull String details) {
        return want(details, true);
    }

    public boolean want(@NonNull String details, boolean want) {
        return team.action.addOffer(
                details,
                want,
                null,
                ""
        );
    }
}
