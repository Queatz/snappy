package com.queatz.snappy.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;
import io.realm.RealmList;

/**
 * Created by jacob on 8/21/15.
 */
public class PeopleNearHereAdapter extends BaseAdapter implements Branchable<Activity> {
    private RealmList<DynamicRealmObject> mRealmList;
    private Context mContext;

    @Override
    public void to(Branch<Activity> branch) {
        Branch.from((Activity) mContext).to(branch);
    }

    public PeopleNearHereAdapter(Context context, RealmList<DynamicRealmObject> realmList) {
        mContext = context;
        Team team = ((MainApplication) mContext.getApplicationContext()).team;
        String me = team.auth.getUser();

        // Remove myself.  Note: This should be a realm query once realm supports geospaital
        for (DynamicRealmObject o : realmList) {
            if (o.getString(Thing.ID).equals(me)) {
                realmList.remove(o);
                break;
            }
        }

        mRealmList = realmList;
    }

    @Override
    public int getCount() {
        if (mRealmList == null) {
            return 0;
        }
        return mRealmList.size();
    }

    @Override
    public DynamicRealmObject getItem(int i) {
        if (mRealmList == null) {
            return null;
        }
        return mRealmList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.person_here_item, parent, false);
        }

        final DynamicRealmObject person = mRealmList.get(position);

        TextView name = (TextView) view.findViewById(R.id.name);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);

        Picasso.with(mContext)
                .load(Functions.getImageUrlForSize(person, (int) Util.px(48)))
                .placeholder(R.color.spacer)
                .into(profile);

        name.setText(person.getString(Thing.FIRST_NAME));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to(new OpenProfileAction(person));
            }
        });

        view.setTag(person);
        ((Activity) mContext).registerForContextMenu(view);

        return view;
    }
}
