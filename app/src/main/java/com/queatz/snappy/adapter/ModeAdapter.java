package com.queatz.snappy.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.contexts.ActivityContext;

import io.realm.DynamicRealmObject;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by jacob on 10/7/17.
 */

public class ModeAdapter extends RealmBaseAdapter<DynamicRealmObject> implements Branchable<ActivityContext> {

    private final Activity context;

    public ModeAdapter(Activity context, @Nullable OrderedRealmCollection<DynamicRealmObject> data) {
        super(data);
        this.context = context;
    }

    @Override
    public void to(Branch<ActivityContext> branch) {
        Branch.from((ActivityContext) context).to(branch);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.mode_item, parent, false);
        }


        final DynamicRealmObject member = getItem(position);

        if (member == null) {
            return view;
        }

        final DynamicRealmObject mode = member.getObject(Thing.SOURCE);

        if (mode == null) {
            return view;
        }

        TextView name = view.findViewById(R.id.name);

        name.setText(mode.getString(Thing.NAME));

        TextView about = view.findViewById(R.id.about);

        about.setText(mode.getString(Thing.ABOUT));

        ImageView photo = view.findViewById(R.id.photo);

        if (mode.getBoolean(Thing.PHOTO)) {
            photo.setImageResource(R.color.spacer);
            Util.setPhotoWithPicasso(mode, (int) Util.px(48), photo);
        } else {
            photo.setImageResource(R.drawable.night);
        }

        return view;
    }
}
