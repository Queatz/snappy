package com.queatz.snappy.ui.card;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.queatz.branch.Branch;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.ui.TextView;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class CommonThingCard implements Card<DynamicRealmObject> {
    @Override
    public View getCard(Context context, DynamicRealmObject thing, View convertView, ViewGroup parent) {
        final Branch<ActivityContext> branch = Branch.from((ActivityContext) context);
        final View view;


        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.common_thing_card, parent, false);
        }

        view.setTag(thing);

        View highlight = view.findViewById(R.id.highlight);

        int color = R.color.gray;

        switch (thing.getString(Thing.KIND)) {
            case ThingKinds.CLUB:
                color = R.color.thing_club;
                break;
            case ThingKinds.RESOURCE:
                color = R.color.thing_resource;
                break;
            case ThingKinds.PROJECT:
                color = R.color.thing_project;
                break;
            case ThingKinds.HUB:
                color = R.color.thing_hub;
                break;
            default:
                color = R.color.gray;
        }

        highlight.setBackgroundColor(context.getResources().getColor(color));

        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (thing.getBoolean(Thing.PHOTO)) {
            photo.setImageDrawable(null);
            Util.setPhotoWithPicasso(thing, parent.getMeasuredWidth(), photo);
        } else {
            photo.setImageDrawable(null);
            photo.setImageResource(R.drawable.night);
            photo.getLayoutParams().height = parent.getMeasuredWidth();
        }

        TextView details = (TextView) view.findViewById(R.id.details);
        TextView name = (TextView) view.findViewById(R.id.name);

        String about = thing.getString(Thing.ABOUT);
        if (about == null || "".equals(about)) {
            details.setText(R.string.no_description_provided);
            details.setTextColor(context.getResources().getColor(R.color.gray));
            details.setTypeface(details.getTypeface(), Typeface.ITALIC);
        } else {
            details.setText(about);
            details.setTextColor(context.getResources().getColor(R.color.text));
            details.setTypeface(details.getTypeface(), Typeface.NORMAL);
        }
        name.setText(thing.getString(Thing.NAME));

        Button viewThing = (Button) view.findViewById(R.id.view);

        viewThing.setText(context.getString(R.string.view_thing, thing.getString(Thing.KIND)));
        viewThing.setTextColor(context.getResources().getColor(color));

        return view;
    }
}
