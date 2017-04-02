package com.queatz.snappy.team;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.makeramen.RoundedTransformationBuilder;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.activity.PersonList;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.ui.EditText;
import com.queatz.snappy.ui.TimeSlider;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Json;
import com.queatz.snappy.util.LocalState;
import com.queatz.snappy.util.ResponseUtil;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 11/23/14.
 */

public class Action {
    public Team team;

    public Action(Team t) {
        team = t;
    }

    public void setSeen(@NonNull final DynamicRealmObject person) {
        if(team.auth.getUser() == null)
            return;

        RealmResults<DynamicRealmObject> recents = team.realm.where("Thing")
                .equalTo("source.id", team.auth.getUser())
                .equalTo("target.id", person.getString(Thing.ID))
                .findAll();

        boolean changed = false;

        team.realm.beginTransaction();

        for(int i = 0; i < recents.size(); i++) {
            DynamicRealmObject recent = recents.get(i);

            if(!recent.getBoolean(Thing.SEEN)) {
                recent.setBoolean(Thing.SEEN, true);
                changed = true;
            }
        }

        if(changed) {
            team.realm.commitTransaction();

            RequestParams params = new RequestParams();
            params.put(Config.PARAM_SEEN, true);

            team.api.post(Config.PATH_EARTH + "/" + person.getString(Thing.ID), params);
        }
        else {
            team.realm.cancelTransaction();
        }
    }

    public void openMessages(@NonNull Activity from, @NonNull final DynamicRealmObject person) {
        openMessages(from, person, null);
    }

    public void openMessages(@NonNull Activity from, @NonNull final DynamicRealmObject person, @Nullable String message) {
        if (from instanceof Person) {
            DynamicRealmObject activityPerson = ((Person) from).getPerson();

            if (activityPerson != null && activityPerson.getString(Thing.ID).equals(person.getString(Thing.ID))) {
                ((Person) from).getSlideScreen().setSlide(Person.SLIDE_MESSAGES);
            }

            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("person", person.getString(Thing.ID));
        bundle.putString("show", "messages");

        if (message != null) {
            bundle.putString("message", message);
        }

        team.view.show(from, Person.class, bundle);
    }

    public void sendMessage(@NonNull final DynamicRealmObject to, @Nullable final String message, @Nullable final Uri photo) {
        if (photo == null && message == null) {
            return;
        }

        final String localId = Util.createLocalId();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);

        if (message != null) {
            params.put(Config.PARAM_MESSAGE, message);
        }

        if (photo != null) try {
            params.put(Config.PARAM_PHOTO, team.context.getContentResolver().openInputStream(photo), photo.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        params.setForceMultipartEntityContentType(true);

        team.realm.beginTransaction();
        DynamicRealmObject o = team.realm.createObject("Thing");
        o.setString(Thing.KIND, "message");
        o.setString(Thing.ID, localId);
        o.setObject(Thing.FROM, team.auth.me());
        o.setObject(Thing.TO, to);

        if (message != null) {
            o.setString(Thing.MESSAGE, message);
        }

        o.setDate(Thing.DATE, new Date());

        team.realm.commitTransaction();

        team.local.updateRecentsForMessage(o);

        team.api.post(Config.PATH_EARTH + "/" + to.getString(Thing.ID) + "/" + Config.PATH_MESSAGE, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Message not sent", Toast.LENGTH_SHORT).show();

                DynamicRealmObject message = team.realm.where("Thing")
                        .equalTo(Thing.ID, localId)
                        .findFirst();

                if (message != null) {
                    team.realm.beginTransaction();
                    message.set(Thing.LOCAL_STATE, LocalState.UNSYNCED);
                    team.realm.commitTransaction();
                }
            }
        });
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

    public void showLikers(@NonNull Activity from, @NonNull final DynamicRealmObject update) {
        Bundle bundle = new Bundle();
        bundle.putString("update", update.getString(Thing.ID));
        bundle.putBoolean("showLikers", true);

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

    public void openDate(@NonNull Activity from, @NonNull final DynamicRealmObject party) {
        Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
        intent.setType("vnd.android.cursor.dir/event");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, party.getDate(Thing.DATE).getTime());
        intent.putExtra(CalendarContract.Events.TITLE, party.getString(Thing.NAME));
        intent.putExtra(CalendarContract.Events.DESCRIPTION, party.getString(Thing.ABOUT));

        if (!party.isNull(Thing.LOCATION)) {
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, party.getObject(Thing.LOCATION).getString(Thing.NAME));
        }

        from.startActivity(intent);
    }

    public void openLocation(@NonNull Activity from, DynamicRealmObject location) {
        if(location == null)
            return;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Functions.getLocationText(location)));

        from.startActivity(intent);
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

    public boolean postSelfUpdate(final Uri photo, final String message) {
        return postSelfUpdate(photo, message, null, null, false);
    }

    public boolean postSelfUpdate(@Nullable final Uri photo, @Nullable final String message, @Nullable final android.location.Location location, @Nullable List<DynamicRealmObject> with, boolean isGoing) {
        RequestParams params = new RequestParams();

        if (photo == null && message == null) {
            return false;
        }

        try {
            params.put(Config.PARAM_THING, team.auth.getUser());

            if (photo != null) {
                params.put(Config.PARAM_PHOTO, team.context.getContentResolver().openInputStream(photo), photo.getPath());
            }

            if (message != null) {
                params.put(Config.PARAM_MESSAGE, message);
            }

            if (location != null) {
                params.put(Config.PARAM_LATITUDE, location.getLatitude());
                params.put(Config.PARAM_LONGITUDE, location.getLongitude());
            }

            if (with != null) {
                List<String> withIds = new ArrayList<>();

                for (DynamicRealmObject person : with) {
                    withIds.add(person.getString(Thing.ID));
                }

                params.put(Config.PARAM_WITH, Json.to(withIds));
                params.put(Config.PARAM_GOING, isGoing);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        // The server expects this whether or not there is an image being uploaded
        params.setForceMultipartEntityContentType(true);

        team.api.post(Config.PATH_EARTH + "?kind=update", params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);

                // If location is null, then probably shared to Village from an external source
                if (location == null) {
                    new OpenProfileAction(team.auth.me()).execute();
                }
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, "Couldn't upload photo", Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    public boolean postCommentOn(final DynamicRealmObject update, final String message) {
        if (update == null || message == null || message.trim().length() < 1) {
            return false;
        }

        final String localId = Util.createLocalId();

        team.realm.beginTransaction();
        DynamicRealmObject o = team.realm.createObject("Thing");
        o.setString(Thing.KIND, "update");
        o.setString(Thing.ID, localId);
        o.setObject(Thing.PERSON, team.auth.me());
        o.setObject(Thing.TARGET, update);
        o.setString(Thing.ABOUT, message);
        o.setString(Thing.ACTION, Config.UPDATE_ACTION_UPTO);
        o.setDate(Thing.DATE, new Date());
        update.getList(Thing.UPDATES).add(o);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_THING, update.getString(Thing.ID));
        params.put(Config.PARAM_MESSAGE, message);
        params.put(Config.PARAM_LOCAL_ID, localId);

        // The server expects this for updates
        params.setForceMultipartEntityContentType(true);

        team.api.post(Config.PATH_EARTH + "?kind=update", params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, "Couldn't post comment", Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    public void deleteThing(@NonNull DynamicRealmObject thing) {
        // TODO keep until delete is in place
        try {
            team.api.post(Config.PATH_EARTH + "/" + thing.getString(Thing.ID) + "/" + Config.PATH_DELETE);

            team.realm.beginTransaction();
            thing.deleteFromRealm();
            team.realm.commitTransaction();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
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

    public void addOffer(@NonNull String details, @Nullable Boolean want, @Nullable Integer price, @Nullable String unit) {
        details = details.trim();

        if(details.isEmpty()) {
            return;
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

        if (want != null) {
            params.put(Config.PARAM_WANT, want);
        }

        team.api.post(Config.PATH_EARTH + "/" + Config.PATH_ME_OFFERS, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, R.string.couldnt_add_offer, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void changeAbout(@NonNull Activity activity) {
        final EditText editText = new EditText(activity);
        int p = (int) Util.px(16);
        editText.setPadding(p, p, p, p);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        editText.setSingleLine(false);
        editText.setHint(R.string.what_are_you_into);

        String about = team.auth.me().getString(Thing.ABOUT);

        if(about == null || about.isEmpty()) {
            about = "";
        }

        editText.setText(about);

        new AlertDialog.Builder(activity).setView(editText)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String about = editText.getText().toString();

                        team.realm.beginTransaction();
                        team.auth.me().setString(Thing.ABOUT, about);
                        team.realm.commitTransaction();

                        RequestParams params = new RequestParams();
                        params.put(Config.PARAM_ABOUT, about);

                        team.api.post(Config.PATH_EARTH + "/" + Config.PATH_ME, params, new Api.Callback() {
                            @Override
                            public void success(String response) {

                            }

                            @Override
                            public void fail(String response) {
                                Toast.makeText(team.context, "Couldn't change what you're into", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .show();

            editText.post(new Runnable() {
                @Override
                public void run() {
                    team.view.keyboard(editText);
                }
            });
    }

    DynamicRealmObject nPendingLocationPhotoChange;

    public void changeLocationPhoto(Activity activity, DynamicRealmObject location) {
        nPendingLocationPhotoChange = location;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, Config.REQUEST_CODE_CHOOSER);
    }

    public void showAbout(Activity activity) {
        View view = View.inflate(activity, R.layout.information, null);

        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    public void likeUpdate(final DynamicRealmObject update) {
        if (Util.liked(update, team.auth.me())) {
            return;
        }

        String localId = Util.createLocalId();

        team.realm.beginTransaction();
        DynamicRealmObject o = team.realm.createObject("Thing");
        o.setString(Thing.KIND, "like");
        o.setString(Thing.ID, localId);
        o.setObject(Thing.SOURCE, team.auth.me());
        o.setObject(Thing.TARGET, update);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);

        team.api.post(Config.PATH_EARTH + "/" + update.getString(Thing.ID) + "/" + Config.PATH_LIKE, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, "Couldn't like " + update.getString(Thing.KIND), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                    Picasso.with(team.context).invalidate(photoUrl);
                }
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, R.string.couldnt_set_photo, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void want(@NonNull String details) {
        want(details, true);
    }

    public void want(@NonNull String details, boolean want) {
        team.action.addOffer(
                details,
                want,
                null,
                ""
        );
    }

    public void offerSomething(@NonNull final Activity activity) {
        final View newOffer = View.inflate(activity, R.layout.new_offer, null);

        final EditText experienceDetails = (EditText) newOffer.findViewById(R.id.details);
        final TimeSlider priceSlider = (TimeSlider) newOffer.findViewById(R.id.price);
        final EditText perUnit = (EditText) newOffer.findViewById(R.id.perWhat);
        final View highlight = newOffer.findViewById(R.id.highlight);

        priceSlider.setPercent(getFreePercent());
        priceSlider.setTextCallback(new TimeSlider.TextCallback() {
            @Override
            public String getText(float percent) {
                Integer price = getPrice(percent);

                if (price == null) {
                    return team.context.getString(R.string.ask);
                }

                if (price < 0) {
                    highlight.setBackgroundResource(R.color.purple);
                    priceSlider.setTextColor(R.color.purple);
                    experienceDetails.setHint(activity.getResources().getString(R.string.what_do_you_want));
                } else {
                    highlight.setBackgroundResource(R.color.green);
                    priceSlider.setTextColor(R.color.green);
                    experienceDetails.setHint(activity.getResources().getString(R.string.what_do_you_offer));
                }

                if (price == 0) {
                    return team.context.getString(R.string.no_bounty);
                }

                return  activity.getString(R.string.for_amount, "$" + Integer.toString(Math.abs(price)));
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(newOffer)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.add, null)
                .setCancelable(true)
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (experienceDetails.getText().toString().isEmpty()) {
                    Toast.makeText(team.context, "Enter description", Toast.LENGTH_SHORT).show();
                    return;
                }

                team.action.addOffer(
                        experienceDetails.getText().toString(),
                        null,
                        getPrice(priceSlider.getPercent()),
                        perUnit.getText().toString()
                );

                dialog.dismiss();
            }
        });

        experienceDetails.post(new Runnable() {
            @Override
            public void run() {
                team.view.keyboard(experienceDetails);
            }
        });

    }

    // TODO move to util
    private float getFreePercent() {
        if (Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled())) {
            return (float) (-Config.PAID_OFFER_PRICE_MIN / (float) (-Config.PAID_OFFER_PRICE_MIN + Config.PAID_OFFER_PRICE_MAX)) * 0.9f;
        } else {
            return (float) (-Config.FREE_OFFER_PRICE_MIN / (float) (-Config.FREE_OFFER_PRICE_MIN + Config.FREE_OFFER_PRICE_MAX)) * 0.9f;
        }
    }

    private Integer getPrice(float percent) {
        if (percent > 0.9f) {
            return null;
        } else {
            percent /= 0.9f;
        }

        Integer price;


        if (Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled())) {
            price = (int) (percent * (Config.PAID_OFFER_PRICE_MAX - Config.PAID_OFFER_PRICE_MIN) + Config.PAID_OFFER_PRICE_MIN);
        } else {
            price = (int) (percent * (Config.FREE_OFFER_PRICE_MAX - Config.FREE_OFFER_PRICE_MIN) + Config.FREE_OFFER_PRICE_MIN);
        }

        if (Math.abs(price) < 200) {
            price = (int) Math.floor(price / 10) * 10;
        } else if (Math.abs(price) < 1000) {
            price = (int) Math.floor(price / 50) * 50;
        } else {
            price = (int) Math.floor(price / 100) * 100;
        }

        return price;
    }

    public void report(Activity activity, final DynamicRealmObject person) {
        final EditText editText = new EditText(activity);
        int p = (int) Util.px(16);
        editText.setPadding(p, p, p, p);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        editText.setHint(R.string.what_went_wrong);
        editText.setSingleLine(false);

        new AlertDialog.Builder(activity).setView(editText)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String report = editText.getText().toString();

                        RequestParams params = new RequestParams();
                        params.put(Config.PARAM_MESSAGE, report);

                        team.api.post(Config.PATH_EARTH + "/" + Config.PATH_ME + "/report/" + person.getString(Thing.ID), params, new Api.Callback() {
                            @Override
                            public void success(String response) {
                                Toast.makeText(team.context, R.string.thanks_for_report, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void fail(String response) {
                                Toast.makeText(team.context, "Failed to report this person", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .show();

        editText.post(new Runnable() {
            @Override
            public void run() {
                team.view.keyboard(editText);
            }
        });
    }

    public void sendFeedback(Activity activity) {
        final EditText editText = new EditText(activity);
        int p = (int) Util.px(16);
        editText.setPadding(p, p, p, p);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        editText.setHint(R.string.write_feedback_here);
        editText.setSingleLine(false);

        new AlertDialog.Builder(activity).setView(editText)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.send_feedback, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String feedback = editText.getText().toString();

                        RequestParams params = new RequestParams();
                        params.put(Config.PARAM_FEEDBACK, feedback);

                        team.api.post(Config.PATH_EARTH + "/feedback", params, new Api.Callback() {
                            @Override
                            public void success(String response) {
                                Toast.makeText(team.context, R.string.thanks, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void fail(String response) {
                                Toast.makeText(team.context, "Failed to send feedback. Ironic...", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .show();

        editText.post(new Runnable() {
            @Override
            public void run() {
                team.view.keyboard(editText);
            }
        });
    }

    public void share(Activity activity, DynamicRealmObject thing) {
        String text;
        String subject;
        String name;

        switch (thing.getString(Thing.KIND)) {
            case "offer":
                name = thing.getObject(Thing.PERSON).getString(Thing.FIRST_NAME) + " " +
                        thing.getObject(Thing.PERSON).getString(Thing.LAST_NAME);

                subject = "Offers by " + name;

                String offerOrRequest = Util.offerIsRequest(thing) ? "wanted" : "offered";

                text = "Check out " + thing.getString(Thing.ABOUT) + " " + offerOrRequest + " by " + name + "\n\n" +
                    Config.VILLAGE_WEBSITE + thing.getObject(Thing.PERSON).getString(Thing.GOOGLE_URL);
                break;
            case "update":
                name = thing.getObject(Thing.PERSON).getString(Thing.FIRST_NAME) + " " +
                        thing.getObject(Thing.PERSON).getString(Thing.LAST_NAME);

                subject = "Updates from " + name;
                text = thing.getString(Thing.ABOUT) + " — " + name + "\n\n" +
                        Config.VILLAGE_WEBSITE + thing.getObject(Thing.PERSON).getString(Thing.GOOGLE_URL);
                break;
            default:
                return;
        }

        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, text);
            activity.startActivity(Intent.createChooser(i, activity.getString(R.string.choose_application)));
        }
        catch(Exception e)
        { //e.toString();
        }
    }

    public void showPrivacyPolicy(Activity activity) {
        View view = View.inflate(activity, R.layout.privacy_policy, null);

        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    public void showTermsOfService(Activity activity) {
        View view = View.inflate(activity, R.layout.terms_of_service, null);

        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    public void addToHomeScreen(final Activity activity, final DynamicRealmObject person) {
        Picasso.with(activity)
                .load(Functions.getImageUrlForSize(person, (int) Util.px(64)))
                .transform(new RoundedTransformationBuilder().oval(true).build())
                .placeholder(R.color.spacer)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Intent shortcutIntent = new Intent(activity, Person.class);
                        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shortcutIntent.putExtra(Config.EXTRA_PERSON_ID, person.getString(Thing.ID));
                        shortcutIntent.putExtra(Config.EXTRA_SHOW, "messages");

                        Intent addIntent = new Intent();
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, person.getString(Thing.FIRST_NAME));
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);

                        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                        activity.sendBroadcast(addIntent);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    public void signout(@NonNull final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.signout_of)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.signout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        team.auth.signout(activity);
                    }
                })
                .show();
    }

    public void showLoginDialog() {
    }
}
