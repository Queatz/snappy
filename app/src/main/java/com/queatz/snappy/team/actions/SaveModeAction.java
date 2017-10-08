package com.queatz.snappy.team.actions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;

/**
 * Created by jacob on 10/7/17.
 */

class SaveModeAction extends AuthenticatedAction {

    private String name;
    private String about;

    public SaveModeAction(@NonNull String name, @Nullable String about) {
        this.name = name;
        this.about = about;
    }

    @Override
    public void whenAuthenticated() {
        name = name.trim();
        about = about.trim();

        if(name.isEmpty() || about.isEmpty()) {
            return;
        }

//        me().getTeam().realm.beginTransaction();
//
//        DynamicRealmObject mode = me().getTeam().realm.createObject("Thing");
//        mode.setString(Thing.KIND, ThingKinds.MODE);
//        mode.setString(Thing.ID, Util.createLocalId());
//        mode.setString(Thing.NAME, name);
//        mode.setString(Thing.ABOUT, about);
//
//        me().getTeam().realm.createObject("Thing");
//        mode.setString(Thing.KIND, ThingKinds.MEMBER);
//        mode.setString(Thing.ID, Util.createLocalId());
//        mode.setObject(Thing.SOURCE, getUser());
//        mode.setObject(Thing.TARGET, mode);
//
//        me().getTeam().realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_KIND, ThingKinds.MODE);
//        params.put(Config.PARAM_LOCAL_ID, mode.getString(Thing.ID));
        params.put(Config.PARAM_ABOUT, about);
        params.put(Config.PARAM_NAME, name);
        params.put(Config.PARAM_IN, getUser().getString(Thing.ID));

        me().getTeam().api.post(Config.PATH_EARTH, params, new Api.Callback() {
            @Override
            public void success(String response) {
                to(new UpdateThings(response));
            }

            @Override
            public void fail(String response) {
                Toast.makeText(me().getTeam().context, R.string.couldnt_add_mode, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
