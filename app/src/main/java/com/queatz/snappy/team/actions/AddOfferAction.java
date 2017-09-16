package com.queatz.snappy.team.actions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class AddOfferAction extends AuthenticatedAction {

    private String details;
    private Boolean want;
    private Integer price;
    private String unit;

    public AddOfferAction(@NonNull String details) {
        this(details, true, null, "");
    }

    public AddOfferAction(@NonNull String details, @Nullable Boolean want, @Nullable Integer price, @Nullable String unit) {
        this.details = details;
        this.want = want;
        this.price = price;
        this.unit = unit;
    }

    @Override
    public void whenAuthenticated() {
        details = details.trim();

        if(details.isEmpty()) {
            return;
        }

        if (me().getTeam().auth.me() == null) {
            return;
        }

        me().getTeam().realm.beginTransaction();
        DynamicRealmObject offer = me().getTeam().realm.createObject("Thing");
        offer.setString(Thing.KIND, "offer");
        offer.setString(Thing.ID, Util.createLocalId());
        offer.setString(Thing.ABOUT, details);

        if (price != null) {
            offer.setInt(Thing.PRICE, price);
        }

        offer.setString(Thing.UNIT, unit);
        offer.setObject(Thing.SOURCE, me().getTeam().auth.me());
        me().getTeam().realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_KIND, "offer");
        params.put(Config.PARAM_LOCAL_ID, offer.getString(Thing.ID));
        params.put(Config.PARAM_DETAILS, details);
        params.put(Config.PARAM_PRICE, price);
        params.put(Config.PARAM_UNIT, unit);
        params.put(Config.PARAM_IN, me().getTeam().auth.me().getString(Thing.ID));

        if (want != null) {
            params.put(Config.PARAM_WANT, want);
        }

        me().getTeam().api.post(Config.PATH_EARTH, params, new Api.Callback() {
            @Override
            public void success(String response) {
                me().getTeam().things.put(response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(me().getTeam().context, R.string.couldnt_add_offer, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
