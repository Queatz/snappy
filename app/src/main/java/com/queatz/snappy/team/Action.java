package com.queatz.snappy.team;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.PersonList;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.things.Bounty;
import com.queatz.snappy.things.Contact;
import com.queatz.snappy.things.Endorsement;
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Like;
import com.queatz.snappy.things.Location;
import com.queatz.snappy.things.Message;
import com.queatz.snappy.things.Offer;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Quest;
import com.queatz.snappy.things.Update;
import com.queatz.snappy.ui.EditText;
import com.queatz.snappy.ui.MiniMenu;
import com.queatz.snappy.ui.TimeSlider;
import com.queatz.snappy.util.ResponseUtil;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

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
        if(team.auth.getUser() == null)
            return;

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

    public void openBounties(@NonNull Activity from) {
        team.view.show(from, com.queatz.snappy.activity.Bounties.class, null);
    }

    /**
     * @deprecated See {@code Offer}
     */
    public void openQuests(@NonNull Activity from) {
        team.view.show(from, com.queatz.snappy.activity.Quests.class, null);
    }

    public void openMessages(@NonNull Activity from, @NonNull final Person person) {
        openMessages(from, person, null);
    }

    public void openMessages(@NonNull Activity from, @NonNull final Person person, @Nullable String message) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getId());
        bundle.putString("show", "messages");

        if (message != null) {
            bundle.putString("message", message);
        }

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

    public void showFollowers(@NonNull Activity from, @NonNull final Person person) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getId());
        bundle.putBoolean("showFollowing", false);

        team.view.show(from, PersonList.class, bundle);
    }

    public void showFollowing(@NonNull Activity from, @NonNull final Person person) {
        Bundle bundle = new Bundle();
        bundle.putString("person", person.getId());
        bundle.putBoolean("showFollowing", true);

        team.view.show(from, PersonList.class, bundle);
    }

    public void showLikers(@NonNull Activity from, @NonNull final Update update) {
        Bundle bundle = new Bundle();
        bundle.putString("update", update.getId());
        bundle.putBoolean("showLikers", true);

        team.view.show(from, PersonList.class, bundle);
    }

    public void followPerson(@NonNull final Person person) {
        final String localId = Util.createLocalId();

        team.realm.beginTransaction();
        Follow o = team.realm.createObject(Follow.class);
        o.setId(localId);
        o.setSource(team.auth.me());
        o.setTarget(person);
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

    public void stopFollowingPerson(@NonNull Person person) {
        Follow follow = team.realm.where(Follow.class)
                .equalTo("source.id", team.auth.getUser())
                .equalTo("target.id", person.getId())
                .findFirst();

        if(follow != null) {
            team.realm.beginTransaction();
            follow.removeFromRealm();
            team.realm.commitTransaction();
        }

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FOLLOW, false);

        team.api.post(String.format(Config.PATH_PEOPLE_ID, person.getId()), params, new Api.Callback() {
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

    public void openDate(@NonNull Activity from, @NonNull final Party party) {
        Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
        intent.setType("vnd.android.cursor.dir/event");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, party.getDate().getTime());
        intent.putExtra(CalendarContract.Events.TITLE, party.getName());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, party.getDetails());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, party.getLocation().getText());

        from.startActivity(intent);
    }

    public void openLocation(@NonNull Activity from, com.queatz.snappy.things.Location location) {
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

    public void joinParty(final Activity activity, @NonNull final String partyId) {
        Party party = team.realm.where(Party.class).equalTo("id", partyId).findFirst();

        if(party == null) {
            team.realm.beginTransaction();
            party = team.realm.createObject(Party.class);
            party.setId(partyId);
            team.realm.commitTransaction();
        }

        joinParty(activity, party);
    }

    public void joinParty(final Activity activity, @NonNull final Party party) {
        String localId = null;

        Join o = team.realm.where(Join.class)
                .equalTo("party.id", party.getId())
                .equalTo("person.id", team.auth.getUser())
                .findFirst();

        if(o == null) {
            localId = Util.createLocalId();

            team.realm.beginTransaction();
            o = team.realm.createObject(Join.class);
            o.setId(localId);
            o.setPerson(team.auth.me());
            o.setParty(party);
            o.setStatus(Config.JOIN_STATUS_REQUESTED);
            team.realm.commitTransaction();
        }

        RequestParams params = new RequestParams();

        if(localId != null)
            params.put(Config.PARAM_LOCAL_ID, localId);

        params.put(Config.PARAM_JOIN, true);

        team.api.post(String.format(Config.PATH_PARTY_ID, party.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (response == null) {
                    return;
                }

                team.things.put(Join.class, response);
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Join failed", Toast.LENGTH_SHORT).show();
            }
        });
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

    public void acceptJoin(@NonNull final String joinId) {
        Join join = team.realm.where(Join.class).equalTo("id", joinId).findFirst();

        if(join == null) {
            team.realm.beginTransaction();
            join = team.realm.createObject(Join.class);
            join.setId(joinId);
            join.setStatus(Config.JOIN_STATUS_REQUESTED);
            team.realm.commitTransaction();
        }

        acceptJoin(join);
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
                team.push.clear("join/" + join.getId() + "/request");
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
                if (!ResponseUtil.isSuccess(response)) {
                    Toast.makeText(team.context, "Hide join failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(team.context, "Hide join failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void hostParty(@NonNull final Activity activity, final String group, final String name, final Date date, final com.queatz.snappy.things.Location location, final String details) {
        RequestParams params = new RequestParams();

        if(group != null && !group.isEmpty())
            params.put("id", group);

        params.put(Config.PARAM_NAME, name);
        params.put(Config.PARAM_DATE, TimeUtil.dateToString(date));
        params.put(Config.PARAM_LOCATION, location.getId() == null ? location.getJson() : location.getId());
        params.put(Config.PARAM_DETAILS, details);

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

    public void showPartiesPostHost(@NonNull final Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("show_post_host_message", true);

        team.view.show(activity, Main.class, bundle);
    }

    public boolean postUpto(final Uri photo, final String message) {
        RequestParams params = new RequestParams();

        try {
            params.put(Config.PARAM_PHOTO, team.context.getContentResolver().openInputStream(photo));
            params.put(Config.PARAM_MESSAGE, message);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        team.api.post(Config.PATH_ME_UPTO, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Update.class, response);
                openProfile(null, team.auth.me());
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, "Couldn't upload photo", Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    public void deleteOffer(@NonNull Offer offer) {
        // TODO keep until delete is in place
        try {
            team.api.delete(String.format(Config.PATH_ME_OFFERS_ID, offer.getId()));

            team.realm.beginTransaction();
            offer.removeFromRealm();
            team.realm.commitTransaction();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    Offer nPendingOfferPhotoChange;

    public void addPhotoToOffer(@NonNull Activity activity, @NonNull Offer offer) {
        nPendingOfferPhotoChange = offer;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, Config.REQUEST_CODE_CHOOSER);
    }

    public void removePhotoFromOffer(@NonNull Offer offer) {
        team.realm.beginTransaction();
        offer.setHasPhoto(false);
        team.realm.commitTransaction();

        team.api.delete(String.format(Config.PATH_OFFER_PHOTO, offer.getId()));
    }

    public void addOffer(@NonNull String details, Integer price, @Nullable String unit) {
        if(details.isEmpty()) {
            return;
        }

        team.realm.beginTransaction();
        Offer offer = team.realm.createObject(Offer.class);
        offer.setId(Util.createLocalId());
        offer.setDetails(details.trim());
        offer.setPrice(price);
        offer.setUnit(unit);
        offer.setPerson(team.auth.me());
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, offer.getId());
        params.put(Config.PARAM_DETAILS, details);
        params.put(Config.PARAM_UNIT, unit);
        params.put(Config.PARAM_PRICE, price);

        team.api.post(Config.PATH_ME_OFFERS, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Offer.class, response);
                Toast.makeText(team.context, "Offer added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, "Couldn't add offer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @deprecated See {@code Offer}
     */
    public void deleteBounty(@NonNull Bounty bounty) {
        try {
            team.api.delete(String.format(Config.PATH_BOUNTY_ID, bounty.getId()), new Api.Callback() {
                @Override
                public void success(String response) {
                    if(!ResponseUtil.isSuccess(response)) {
                        Toast.makeText(team.context, "Couldn't cancel bounty", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void fail(String response) {
                    Toast.makeText(team.context, "Couldn't cancel bounty", Toast.LENGTH_SHORT).show();
                }
            });

            team.realm.beginTransaction();
            bounty.removeFromRealm();
            team.realm.commitTransaction();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * @deprecated See {@code Offer}
     */
    public void postBounty(@NonNull String details, int price) {
        if(details.isEmpty()) {
            return;
        }

        team.realm.beginTransaction();
        Bounty bounty = team.realm.createObject(Bounty.class);
        bounty.setId(Util.createLocalId());
        bounty.setDetails(details);
        bounty.setPrice(price);
        bounty.setStatus(Config.BOUNTY_STATUS_OPEN);
        bounty.setPosted(new Date());
        bounty.setPoster(team.auth.me());
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, bounty.getId());
        params.put(Config.PARAM_DETAILS, details);
        params.put(Config.PARAM_PRICE, price);

        team.api.post(Config.PATH_BOUNTIES, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Bounty.class, response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, "Couldn't post bounty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @deprecated See {@code Offer}
     */
    public void finishBounty(@NonNull final Activity activity, @NonNull final Bounty bounty) {
        team.realm.beginTransaction();
        bounty.setStatus(Config.BOUNTY_STATUS_FINISHED);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FINISH, true);

        team.api.post(String.format(Config.PATH_BOUNTY_ID, bounty.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (ResponseUtil.isSuccess(response)) {
                    Toast.makeText(team.context, "Bounty finished", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(team.context, "Bounty couldn't be finished", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(String response) {
                // TODO revert claimed state
                Toast.makeText(team.context, "Bounty couldn't be finished", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @deprecated See {@code Offer}
     */
    public void claimBounty(@NonNull final Activity activity, @NonNull final Bounty bounty) {
        boolean isMine = false;

        for (Person person : bounty.getPeople()) {
            if(team.auth.me().getId().equals(person.getId())) {
                isMine = true;
                break;
            }
        }

        if(isMine) {
            if(Config.BOUNTY_STATUS_FINISHED.equals(bounty.getStatus())) {
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.you_finished_this_bounty)
                        .setPositiveButton(R.string.message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openMessages(activity, bounty.getPoster());
                            }
                        })
                        .show();
            }
            else {
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.you_claimed_this_bounty)
                        .setNegativeButton(R.string.message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openMessages(activity, bounty.getPoster());
                            }
                        })
                        .setPositiveButton(R.string.finish, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishBounty(activity, bounty);
                            }
                        })
                        .show();
            }

            return;
        }

        new AlertDialog.Builder(activity)
                .setMessage(R.string.claim_this_bounty)
                .setNeutralButton(R.string.message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openMessages(activity, bounty.getPoster());
                    }
                })
                .setPositiveButton(R.string.claim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        team.realm.beginTransaction();
                        bounty.setStatus(Config.BOUNTY_STATUS_CLAIMED);
                        bounty.getPeople().add(team.auth.me());
                        team.realm.commitTransaction();

                        RequestParams params = new RequestParams();
                        params.put(Config.PARAM_CLAIM, true);

                        team.api.post(String.format(Config.PATH_BOUNTY_ID, bounty.getId()), params, new Api.Callback() {
                            @Override
                            public void success(String response) {
                                if (ResponseUtil.isSuccess(response)) {
                                    Toast.makeText(team.context, "Bounty claimed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(team.context, "Bounty couldn't be claimed", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void fail(String response) {
                                // TODO revert claimed state
                                Toast.makeText(team.context, "Bounty couldn't be claimed", Toast.LENGTH_SHORT).show();
                            }
                        });

                        openMessages(activity, bounty.getPoster());
                    }
                })
                .show();
    }

    /**
     * @deprecated See {@code Offer}
     */
    public boolean newQuest(@NonNull String name, @NonNull String details, @NonNull String reward, String time, int teamSize) {
        if (name.trim().isEmpty()) {
            Toast.makeText(team.context, team.context.getString(R.string.enter_quest_name), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (details.trim().isEmpty()) {
            Toast.makeText(team.context, team.context.getString(R.string.describe_the_quest), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (reward.trim().isEmpty()) {
            Toast.makeText(team.context, team.context.getString(R.string.specify_reward), Toast.LENGTH_SHORT).show();
            return false;
        }

        team.realm.beginTransaction();
        Quest quest = team.realm.createObject(Quest.class);
        quest.setId(Util.createLocalId());
        quest.setName(name);
        quest.setDetails(details);
        quest.setReward(reward);
        quest.setStatus(Config.QUEST_STATUS_OPEN);
        quest.setOpened(new Date());
        quest.setHost(team.auth.me());
        quest.setTeamSize(teamSize);
        quest.setTime(time);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, quest.getId());
        params.put(Config.PARAM_NAME, name);
        params.put(Config.PARAM_DETAILS, details);
        params.put(Config.PARAM_REWARD, reward);
        params.put(Config.PARAM_TEAM_SIZE, teamSize);
        params.put(Config.PARAM_TIME, time);

        team.api.post(Config.PATH_QUEST, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Quest.class, response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, "Couldn't open new quest", Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    /**
     * @deprecated See {@code Offer}
     */
    public void startQuest(@NonNull final Activity activity, @NonNull final Quest quest) {
        team.realm.beginTransaction();
        quest.setStatus(Config.QUEST_STATUS_STARTED);
        quest.getTeam().add(team.auth.me());
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_START, true);

        team.api.post(String.format(Config.PATH_QUEST_ID, quest.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (!ResponseUtil.isSuccess(response)) {
                    Toast.makeText(team.context, "Quest couldn't be started", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(String response) {
                // TODO revert started state
                Toast.makeText(team.context, "Quest couldn't be started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @deprecated See {@code Offer}
     */
    public void markQuestComplete(@NonNull Quest quest) {
        team.realm.beginTransaction();
        quest.setStatus(Config.QUEST_STATUS_COMPLETE);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_COMPLETE, true);

        team.api.post(String.format(Config.PATH_QUEST_ID, quest.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (ResponseUtil.isSuccess(response)) {
                    Toast.makeText(team.context, "Quest completed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(team.context, "Quest couldn't be completed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(String response) {
                // TODO revert started state
                Toast.makeText(team.context, "Quest couldn't be completed", Toast.LENGTH_SHORT).show();
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

        String about = team.auth.me().getAbout();

        if(about == null || about.isEmpty()) {
            about = "";
        }

        editText.setText(about);

        new AlertDialog.Builder(activity).setView(editText)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String about = editText.getText().toString();

                        team.realm.beginTransaction();
                        team.auth.me().setAbout(about);
                        team.realm.commitTransaction();

                        RequestParams params = new RequestParams();
                        params.put(Config.PARAM_ABOUT, about);

                        team.api.post(Config.PATH_ME, params, new Api.Callback() {
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


    Location nPendingLocationPhotoChange;

    public void changeLocationPhoto(Activity activity, Location location) {
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

    public void likeUpdate(Update update) {
        if (Util.liked(update, team.auth.me())) {
            return;
        }

        String localId = Util.createLocalId();

        team.realm.beginTransaction();
        Like o = team.realm.createObject(Like.class);
        o.setId(localId);
        o.setSource(team.auth.me());
        o.setTarget(update);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);
        params.put(Config.PARAM_LIKE, true);

        team.api.post(String.format(Config.PATH_UPDATE_ID, update.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Like.class, response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, "Couldn't like update", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void endorse(@NonNull final Activity activity, @NonNull final Offer offer) {
        if (offer.getPerson() == null) {
            Log.w(Config.LOG_TAG, "Offer has no person!");
            return;
        }

        if (Util.endorsed(offer, team.auth.me())) {
            new AlertDialog.Builder(activity)
                    .setMessage(Util.fancyFormat(R.string.youve_already_endorsed_person, offer.getPerson().getFirstName(), offer.getDetails()))
                    .setCancelable(true)
                    .setPositiveButton(team.context.getString(R.string.ok), null)
                    .show().setCanceledOnTouchOutside(true);

            return;
        }

        new AlertDialog.Builder(activity)
                .setMessage(Util.fancyFormat(R.string.endorse_offer, offer.getPerson().getFirstName(), offer.getDetails()))
                .setNegativeButton(R.string.nope, null)
                .setCancelable(true)
                .setPositiveButton(team.context.getString(R.string.endorse), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doEndorse(offer);
                    }
                })
                .show().setCanceledOnTouchOutside(true);
    }

    private void doEndorse(@NonNull final Offer offer) {
        String localId = Util.createLocalId();

        team.realm.beginTransaction();
        Endorsement o = team.realm.createObject(Endorsement.class);
        o.setId(localId);
        o.setSource(team.auth.me());
        o.setTarget(offer);
        team.realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);

        team.api.post(String.format(Config.PATH_OFFER_ID_ENDORSE, offer.getId()), params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(Offer.class, response);
                Toast.makeText(team.context, team.context.getString(R.string.you_endorsed_person, offer.getPerson().getFirstName()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, R.string.couldnt_endorse, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showEndorsers(@NonNull final Activity activity, @NonNull final Offer offer) {
        Bundle bundle = new Bundle();
        bundle.putString("offer", offer.getId());
        bundle.putBoolean("showEndorsers", true);

        team.view.show(activity, PersonList.class, bundle);
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
                        uploadPhoto(String.format(Config.PATH_LOCATION_PHOTO, nPendingLocationPhotoChange.getId()), photo);
                    } else if(nPendingOfferPhotoChange != null) {
                        uploadPhoto(String.format(Config.PATH_OFFER_PHOTO, nPendingOfferPhotoChange.getId()), photo);
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
            id = nPendingLocationPhotoChange.getId();
        } else if (nPendingOfferPhotoChange != null) {
            id = nPendingOfferPhotoChange.getId();
        } else {
            id = null;
        }

        team.api.put(path, params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (id != null) {
                    String photoUrl = Config.API_URL + String.format(Config.PATH_LOCATION_PHOTO + "?s=64&auth=" + team.auth.getAuthParam(), id);
                    Picasso.with(team.context).invalidate(photoUrl);
                }
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, R.string.couldnt_set_photo, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void offerSomething(Activity activity) {
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
                } else {
                    highlight.setBackgroundResource(R.color.green);
                    priceSlider.setTextColor(R.color.green);
                }

                if (price == 0) {
                    return team.context.getString(R.string.free);
                }

                return  (price < 0 ? "+" : "") + "$" + Integer.toString(Math.abs(price));
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(newOffer)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.add_experience, null)
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
            return (float) -Config.PAID_OFFER_PRICE_MIN / (float) (-Config.PAID_OFFER_PRICE_MIN + Config.PAID_OFFER_PRICE_MAX);
        } else {
            return (float) -Config.FREE_OFFER_PRICE_MIN / (float) (-Config.FREE_OFFER_PRICE_MIN + Config.FREE_OFFER_PRICE_MAX);
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
}
