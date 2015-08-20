package com.queatz.snappy.team;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.queatz.snappy.R;
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Location;
import com.queatz.snappy.things.Person;

/**
 * Created by jacob on 8/20/15.
 */
public class Menu {
    public Team team;

    public Menu(Team team) {
        this.team = team;
    }

    public void make(Object object, ContextMenu menu, ContextMenu.ContextMenuInfo info) {
        if(object instanceof Location) {
            menu.add(R.string.change_photo);
        }
        else if(object instanceof Person) {
            Person person = (Person) object;

            if(!person.getId().equals(team.auth.me().getId())) {
                //TODO make sure follow for you -> them is leaded when loading profile...

                Follow follow = team.realm.where(Follow.class)
                        .equalTo("person.id", team.auth.getUser())
                        .equalTo("following.id", person.getId())
                        .findFirst();

                menu.add(follow == null ? R.string.follow : R.string.stop_following);
            }
        }
    }

    public boolean choose(Activity activity, Object object, MenuItem item) {
        if(object instanceof com.queatz.snappy.things.Location) {
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

        return true;
    }
}
