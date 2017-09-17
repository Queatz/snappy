package com.queatz.snappy.team.actions;

import android.os.Bundle;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.TimeUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class HostPartyAction extends ActivityAction {

    private String group;
    private String name;
    private Date date;
    private DynamicRealmObject location;
    private String details;

    public HostPartyAction(String group, String name, Date date, DynamicRealmObject location, String details) {
        this.group = group;
        this.name = name;
        this.date = date;
        this.location = location;
        this.details = details;
    }

    @Override
    protected void execute() {
        RequestParams params = new RequestParams();

        if(group != null && !group.isEmpty()) {
            params.put(Thing.ID, group);
        }

        params.put(Config.PARAM_KIND, ThingKinds.PARTY);

        try {
            params.put(Config.PARAM_NAME, URLEncoder.encode(name, "UTF-8"));
            params.put(Config.PARAM_DATE, URLEncoder.encode(TimeUtil.dateToString(date), "UTF-8"));
            params.put(Config.PARAM_LOCATION, location.getString(Thing.ID) == null ? URLEncoder.encode(Functions.getLocationJson(location).toString(), "UTF-8") : location.getString(Thing.ID));
            params.put(Config.PARAM_DETAILS, URLEncoder.encode(details, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        getTeam().api.post(Config.PATH_EARTH + "?" + params.toString(), null, new Api.Callback() {
            @Override
            public void success(String response) {
                to(new UpdateThings(response));
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(getTeam().context, "Host party failed", Toast.LENGTH_SHORT).show();
            }
        });

        Bundle bundle = new Bundle();
        bundle.putBoolean("show_post_host_message", true);
        getTeam().view.show(me().getActivity(), Main.class, bundle);
    }
}
