package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.email.EarthEmail;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.api.Interfaceable;
import com.queatz.snappy.view.SuccessView;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/11/16.
 */
public class FeedbackInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        return null;
    }

    @Override
    public String post(EarthAs as) {
        as.requireUser();

        EarthThing jacob = new EarthStore(as).get(Config.JACOB);
        String feedback = as.getRequest().getParameter("feedback");

        new EarthEmail().sendRawEmail(as.getUser(), jacob, "Village Feedback", feedback);

        return new SuccessView(true).toJson();
    }
}
