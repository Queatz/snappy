package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 8/20/17.
 */

public class ClubView extends CommonThingView {
    public ClubView(EarthAs as, EarthThing club) {
        this(as, club, EarthView.DEEP);
    }

    public ClubView(EarthAs as, EarthThing club, EarthView view) {
        super(as, club, view);
    }
}
