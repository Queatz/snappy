package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthEmail;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.views.SuccessView;

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
        Entity jacob = new EarthStore(as).get("-697803823443327660");
        String feedback = as.getRequest().getParameter("feedback");

        new EarthEmail().sendRawEmail(as.getUser(), jacob, "Village Feedback", feedback);

        return new SuccessView(true).toJson();
    }
}
