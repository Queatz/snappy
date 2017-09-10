package com.queatz.snappy.team.actions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class AddToHomeScreenAction extends ActivityAction {

    private final DynamicRealmObject person;

    public AddToHomeScreenAction(DynamicRealmObject person) {
        this.person = person;
    }

    @Override
    protected void execute() {
        Images.with(me().getActivity())
                .load(Functions.getImageUrlForSize(person, (int) Util.px(64)))
                .transform(new RoundedTransformationBuilder().oval(true).build())
                .placeholder(R.color.spacer)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Intent shortcutIntent = new Intent(me().getActivity(), Person.class);
                        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shortcutIntent.putExtra(Config.EXTRA_PERSON_ID, person.getString(Thing.ID));
                        shortcutIntent.putExtra(Config.EXTRA_SHOW, "messages");

                        Intent addIntent = new Intent();
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, person.getString(Thing.FIRST_NAME));
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);

                        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                        me().getActivity().sendBroadcast(addIntent);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }
}
