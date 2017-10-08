package com.queatz.snappy.team.actions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;

import io.realm.DynamicRealmObject;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

/**
 * Created by jacob on 10/7/17.
 */

public class ViewModeAction extends ActivityAction {

    private final DynamicRealmObject mode;
    private boolean modeToggle = false;

    public ViewModeAction(DynamicRealmObject mode) {
        this.mode = mode;
    }

    @Override
    protected void execute() {
        final View view = View.inflate(me().getActivity(), R.layout.view_mode, null);
        final ImageView photo = view.findViewById(R.id.photo);
        final TextView name = view.findViewById(R.id.name);
        final TextView about = view.findViewById(R.id.about);

        name.setText(mode.getString(Thing.NAME));
        about.setText(mode.getString(Thing.ABOUT));

        final AlertDialog dialog = new AlertDialog.Builder(me().getActivity())
                .setView(view)
                .setNeutralButton(R.string.turn_on_mode, null)
                .setPositiveButton(R.string.got_it, null)
                .setCancelable(true)
                .show();

        final RealmResults<DynamicRealmObject> m = getTeam().realm.where("Thing")
                .equalTo(Thing.KIND, ThingKinds.MEMBER)
                .equalTo("target.id", getTeam().auth.getUser())
                .equalTo("source.id", mode.getString(Thing.ID))
                .findAllAsync();

        m.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<DynamicRealmObject>>() {
            @Override
            public void onChange(RealmResults<DynamicRealmObject> dynamicRealmObjects, OrderedCollectionChangeSet orderedCollectionChangeSet) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        updateDlgBtn(dialog);
                    }
                });
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                to(new ChangePhotoAction(mode));
            }
        });

        view.post(new Runnable() {
            @Override
            public void run() {
                if (mode.getBoolean(Thing.PHOTO)) {
                    photo.setImageDrawable(null);
                    Util.setPhotoWithPicasso(mode, view.getMeasuredWidth(), photo);
                } else {
                    photo.setImageDrawable(null);
                    photo.setImageResource(R.drawable.night);
                    photo.getLayoutParams().height = view.getMeasuredWidth();
                }
            }
        });

        updateDlgBtn(dialog);

        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(getTeam().context.getResources().getColor(R.color.thing_mode));

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                to(new ModeChangeAction(mode, modeToggle));
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                m.removeAllChangeListeners();
            }
        });
    }

    private void updateDlgBtn(AlertDialog dialog) {
        Button btn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);

        if (!Util.isModeOn(mode, getTeam().auth.me())) {
            btn.setTextColor(getTeam().context.getResources().getColor(R.color.green));
            btn.setText(R.string.turn_on_mode);
            modeToggle = true;
        } else {
            btn.setTextColor(getTeam().context.getResources().getColor(R.color.gray));
            btn.setText(R.string.turn_off_mode);
            modeToggle = false;
        }
    }
}
