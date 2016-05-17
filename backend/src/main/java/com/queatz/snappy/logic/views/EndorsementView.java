package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/14/16.
 */
public class EndorsementView extends LinkView {
    public EndorsementView(Entity endorsement) {
        this(endorsement, EarthView.DEEP);
    }

    public EndorsementView(Entity endorsement, EarthView view) {
        super(endorsement, view);
    }
}
