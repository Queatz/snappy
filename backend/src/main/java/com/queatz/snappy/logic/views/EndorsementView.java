package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/14/16.
 */
public class EndorsementView extends LinkView {
    public EndorsementView(EarthAs as, Entity endorsement) {
        this(as, endorsement, EarthView.DEEP);
    }

    public EndorsementView(EarthAs as, Entity endorsement, EarthView view) {
        super(as, endorsement, view);
    }
}
