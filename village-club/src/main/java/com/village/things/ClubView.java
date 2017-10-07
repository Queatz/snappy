package com.village.things;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.view.EarthView;

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
