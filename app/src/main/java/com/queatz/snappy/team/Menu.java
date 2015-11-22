package com.queatz.snappy.team;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.Toast;

import com.queatz.snappy.R;
import com.queatz.snappy.activity.HostParty;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.things.Bounty;
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Location;
import com.queatz.snappy.things.Offer;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Quest;

/**
 * Created by jacob on 8/20/15.
 */
public class Menu {
    public Team team;

    public Menu(Team team) {
        this.team = team;
    }

    public void make(Object object, ContextMenu menu, ContextMenu.ContextMenuInfo info) {
        if (object instanceof String) {
            switch ((String) object) {
                case "profile menu":
                    menu.add(R.string.about);

                    if(Config.HOSTING_ENABLED_AVAILABLE.equals(team.buy.hostingEnabled())) {
                        menu.add(R.string.buy_and_host);
                    }

                    menu.add(R.string.logout);
                    break;
            }
        }
        else if (object instanceof Location) {
            menu.add(R.string.change_photo);
        }
        else if (object instanceof Person) {
            Person person = (Person) object;

            if (!person.getId().equals(team.auth.me().getId())) {
                //TODO make sure follow for you -> them is leaded when loading profile...

                Follow follow = team.realm.where(Follow.class)
                        .equalTo("source.id", team.auth.getUser())
                        .equalTo("target.id", person.getId())
                        .findFirst();

                menu.add(follow == null ? R.string.follow : R.string.stop_following);
            }
        }
        else if (object instanceof Offer) {
            menu.add(R.string.stop_offering);
        }
        else if (object instanceof Bounty) {
            menu.add(R.string.cancel_bounty);
        }
        else if (object instanceof Quest) {
            menu.add(R.string.view_contact);
        }
    }

    public boolean choose(final Activity activity, Object object, final MenuItem item) {
        if(object instanceof String) {
            switch ((String) object) {
                case "profile menu":
                    if (team.context.getString(R.string.logout).equals(item.getTitle())) {
                        team.auth.logout(activity);
                    } else if (team.context.getString(R.string.about).equals(item.getTitle())) {
                        team.action.showAbout(activity);
                    }
                    else if(team.context.getString(R.string.buy_and_host).equals(item.getTitle())) {
                        team.buy.callback(new Buy.PurchaseCallback() {
                            @Override
                            public void onSuccess() {
                                team.view.show(activity, HostParty.class, null);
                            }

                            @Override
                            public void onError() {
                                Toast.makeText(team.context, team.context.getString(R.string.buy_didnt_work), Toast.LENGTH_SHORT).show();
                            }
                        });

                        team.buy.buy(activity);
                    }

                    break;
            }
        }
        else if(object instanceof com.queatz.snappy.things.Location) {
            if(team.context.getString(R.string.change_photo).equals(item.getTitle())) {
                team.action.changeLocationPhoto(activity, (com.queatz.snappy.things.Location) object);
            }
        }
        else if(object instanceof Person) {
            if(team.context.getString(R.string.follow).equals(item.getTitle())) {
                team.action.followPerson((Person) object);
            }
            else if(team.context.getString(R.string.stop_following).equals(item.getTitle())) {
                team.action.stopFollowingPerson((Person) object);
            }
        }
        else if(object instanceof Offer) {
            if(team.context.getString(R.string.stop_offering).equals(item.getTitle())) {
                team.action.deleteExperience((Offer) object);
            }
        }
        else if(object instanceof Bounty) {
            if(team.context.getString(R.string.cancel_bounty).equals(item.getTitle())) {
                team.action.deleteBounty((Bounty) object);
            }
        }
        else if(object instanceof Quest) {
            if(team.context.getString(R.string.view_contact).equals(item.getTitle())) {
                team.action.openProfile(activity, ((Quest) object).getHost());
            }
        }

        return true;
    }
}
