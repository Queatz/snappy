package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Location;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Update;
import com.squareup.picasso.Picasso;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/18/15.
 */
public class PersonUptoAdapter extends RealmBaseAdapter<Update> {
    public PersonUptoAdapter(Context context, RealmResults<Update> realmResults) {
        super(context, realmResults, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.person_upto_item, parent, false);
        }

        Team team = ((MainApplication) context.getApplicationContext()).team;

        Update update = realmResults.get(position);
        Person person = update.getPerson();
        Location location = update.getParty() != null ? update.getParty().getLocation() : null;

        if(person != null) {
            int s = (int) Util.px(64);

            Picasso.with(context)
                    .load(location == null ? person.getImageUrlForSize(s) : Util.locationPhoto(location, s))
                    .placeholder(location == null ? R.color.spacer : R.drawable.location)
                    .into((ImageView) view.findViewById(R.id.profile));
        }

        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (Config.UPDATE_ACTION_UPTO.equals(update.getAction())) {
            String photoUrl = Config.API_URL + String.format(Config.PATH_UPDATE_PHOTO + "?s=" + (parent.getMeasuredWidth() / 2) + "&auth=" + team.auth.getAuthParam(), update.getId());

            photo.setVisibility(View.VISIBLE);

            photo.setImageDrawable(null);

            Picasso.with(context).cancelRequest(photo);

            Picasso.with(context)
                    .load(photoUrl)
                    .placeholder(R.color.spacer)
                    .into(photo);

            if(update.getMessage() == null || update.getMessage().isEmpty()) {
                view.findViewById(R.id.details).setVisibility(View.GONE);
            }
            else {
                view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            }
        }
        else {
            view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            photo.setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.text)).setText(
                Util.getUpdateText(update)
        );

        return view;
    }
}
