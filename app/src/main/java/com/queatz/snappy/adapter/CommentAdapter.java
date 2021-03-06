package com.queatz.snappy.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
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
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;
import com.queatz.snappy.util.TimeUtil;

import io.realm.DynamicRealmObject;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by jacob on 10/15/16.
 */

public class CommentAdapter extends RealmBaseAdapter<DynamicRealmObject> implements Branchable<ActivityContext> {

    private final Context context;

    @Override
    public void to(Branch<ActivityContext> branch) {
        Branch.from((ActivityContext) context).to(branch);
    }

    public CommentAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<DynamicRealmObject> data) {
        super(data);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.comment_item, parent, false);
        }

        DynamicRealmObject comment = getItem(position).getObject(Thing.SOURCE);

        final DynamicRealmObject person = comment.getObject(Thing.SOURCE);

        if(person != null) {
            int s = (int) Util.px(32);

            ImageView profile = (ImageView) view.findViewById(R.id.profile);

            Images.with(context)
                    .load(Functions.getImageUrlForSize(person, s))
                    .placeholder(R.color.spacer)
                    .into(profile);

            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    to(new OpenProfileAction(person));
                }
            });

            TextView type = (TextView) view.findViewById(R.id.name);
            type.setText(Functions.getFullName(person));

            TextView time = (TextView) view.findViewById(R.id.time);
            time.setText(TimeUtil.agoDate(comment.getDate(Thing.DATE)));
        }

        ((TextView) view.findViewById(R.id.comment)).setText(comment.getString(Thing.ABOUT));

        return view;
    }
}
