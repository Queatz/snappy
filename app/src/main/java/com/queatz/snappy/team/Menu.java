package com.queatz.snappy.team;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.Toast;

import com.queatz.snappy.R;
import com.queatz.snappy.activity.HostParty;
import com.queatz.snappy.shared.Config;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 8/20/15.
 */
public class Menu {
    public Team team;

    public Menu(Team team) {
        this.team = team;
    }

    public void make(Object object, ContextMenu menu, ContextMenu.ContextMenuInfo info) {
        String kind = null;

        if (object instanceof DynamicRealmObject) {
            kind = ((DynamicRealmObject) object).getString(Thing.KIND);
        }

        if (object instanceof String) {
            switch ((String) object) {
                case "profile menu":
                    menu.add(R.string.about);
                    menu.add(R.string.host_a_party);

                    if(Config.HOSTING_ENABLED_AVAILABLE.equals(team.buy.hostingEnabled())) {
                        menu.add(R.string.buy_and_host);
                    }

                    menu.add(R.string.logout);
                    break;
            }
        }
        else if ("location".equals(kind)) {
            menu.add(R.string.change_photo);
        }
        else if ("person".equals(kind)) {
            if (!((DynamicRealmObject) object).getString(Thing.ID).equals(team.auth.me().getString(Thing.ID))) {
                //TODO make sure follow for you -> them is leaded when loading profile...

                DynamicRealmObject follow = team.realm.where("Thing")
                        .equalTo(Thing.KIND, "follower")
                        .equalTo("source.id", team.auth.getUser())
                        .equalTo("target.id", ((DynamicRealmObject) object).getString(Thing.ID))
                        .findFirst();

                menu.add(R.string.report_this_person);
                menu.add(follow == null ? R.string.follow : R.string.stop_following);
            }
        }
        else if ("offer".equals(kind)) {
            DynamicRealmObject offer = (DynamicRealmObject) object;

            if (offer.getBoolean(Thing.PHOTO)) {
                menu.add(R.string.change_photo);
                menu.add(R.string.remove_photo);
            } else {
                menu.add(R.string.add_photo);
            }

            menu.add(R.string.stop_offering);
        }
    }

    public boolean choose(final Activity activity, Object object, final MenuItem item) {
        String kind = null;

        if (object instanceof DynamicRealmObject) {
            kind = ((DynamicRealmObject) object).getString(Thing.KIND);
        }

        if(object instanceof String) {
            switch ((String) object) {
                case "profile menu":
                    if (team.context.getString(R.string.logout).equals(item.getTitle())) {
                        team.auth.logout(activity);
                    } else if (team.context.getString(R.string.about).equals(item.getTitle())) {
                        team.action.showAbout(activity);
                    } else if (team.context.getString(R.string.host_a_party).equals(item.getTitle())) {
                        team.view.show(activity, HostParty.class, null);
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
        else if("location".equals(kind)) {
            if(team.context.getString(R.string.change_photo).equals(item.getTitle())) {
                team.action.changeLocationPhoto(activity, ((DynamicRealmObject) object));
            }
        }
        else if("person".equals(kind)) {
            if(team.context.getString(R.string.follow).equals(item.getTitle())) {
                team.action.followPerson(((DynamicRealmObject) object));
            }
            else if(team.context.getString(R.string.stop_following).equals(item.getTitle())) {
                team.action.stopFollowingPerson(((DynamicRealmObject) object));
            } else if (team.context.getString(R.string.report_this_person).equals(item.getTitle())) {
                team.action.report(activity, (DynamicRealmObject) object);
            }
        }
        else if("offer".equals(kind)) {
            if(team.context.getString(R.string.stop_offering).equals(item.getTitle())) {
                team.action.deleteOffer(((DynamicRealmObject) object));
            } else if(team.context.getString(R.string.add_photo).equals(item.getTitle()) || team.context.getString(R.string.change_photo).equals(item.getTitle())) {
                team.action.addPhotoToOffer(activity, ((DynamicRealmObject) object));
            } else if(team.context.getString(R.string.remove_photo).equals(item.getTitle())) {
                team.action.removePhotoFromOffer(((DynamicRealmObject) object));
            }
        }

        return true;
    }
}
