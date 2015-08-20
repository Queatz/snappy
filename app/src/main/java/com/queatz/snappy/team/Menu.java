package com.queatz.snappy.team;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.queatz.snappy.R;
import com.queatz.snappy.things.Location;

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
    }

    public boolean choose(Activity activity, Object object, MenuItem item) {
        if(object instanceof com.queatz.snappy.things.Location) {
            if(team.context.getString(R.string.change_photo).equals(item.getTitle())) {
                team.action.changeLocationPhoto(activity, (com.queatz.snappy.things.Location) object);
            }
        }

        return true;
    }
}
