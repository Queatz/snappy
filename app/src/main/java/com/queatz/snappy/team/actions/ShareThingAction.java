package com.queatz.snappy.team.actions;

import android.content.Intent;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class ShareThingAction extends ActivityAction {

    private final DynamicRealmObject thing;

    public ShareThingAction(DynamicRealmObject thing) {
        this.thing = thing;
    }

    @Override
    public void execute() {
        String text;
        String subject;
        String name;

        switch (thing.getString(Thing.KIND)) {
            case "offer":
                name = thing.getObject(Thing.SOURCE).getString(Thing.FIRST_NAME) + " " +
                        thing.getObject(Thing.SOURCE).getString(Thing.LAST_NAME);

                subject = "Offers by " + name;

                String offerOrRequest = Util.offerIsRequest(thing) ? "wanted" : "offered";

                text = "Check out " + thing.getString(Thing.ABOUT) + " " + offerOrRequest + " by " + name + "\n\n" +
                        Config.VILLAGE_WEBSITE + thing.getObject(Thing.SOURCE).getString(Thing.GOOGLE_URL);
                break;
            case "update":
                name = thing.getObject(Thing.SOURCE).getString(Thing.FIRST_NAME) + " " +
                        thing.getObject(Thing.SOURCE).getString(Thing.LAST_NAME);

                subject = "Updates from " + name;
                text = thing.getString(Thing.ABOUT) + " — " + name + "\n\n" +
                        Config.VILLAGE_WEBSITE + thing.getObject(Thing.SOURCE).getString(Thing.GOOGLE_URL);
                break;
            default:
                return;
        }

        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, text);
            me().getActivity().startActivity(Intent.createChooser(i, me().getActivity().getString(R.string.choose_application)));
        }
        catch(Exception e)
        { //e.toString();
        }
    }
}
