package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.mines.ContactMine;
import com.queatz.snappy.logic.mines.FollowerMine;
import com.queatz.snappy.logic.mines.MemberMine;
import com.queatz.snappy.logic.mines.UpdateMine;
import com.queatz.snappy.shared.Config;

import java.util.List;

/**
 * Created by jacob on 5/22/16.
 */
public class CommonThingView extends ThingView {

    /**
     * List of members of this thing of all kinds.
     */
    final List<Viewable> members;

    /**
     * Number of backers of this thing.
     */
    final int backers;

    public CommonThingView(EarthAs as, EarthThing thing) {
        this(as, thing, EarthView.DEEP);
    }

    public CommonThingView(EarthAs as, EarthThing thing, EarthView view) {
        super(as, thing, view);

        backers = use(FollowerMine.class).countFollowers(thing);

        switch (view) {
            case DEEP:
                final MemberMine memberMine = use(MemberMine.class);

                final List<EarthThing> pool = memberMine.byThingWithStatus(thing, Config.MEMBER_STATUS_ACTIVE);

                if (pool != null) {
                    this.members = new EntityListView(as, pool, EarthView.SHALLOW).asList();
                } else {
                    this.members = null;
                }
                break;
            default:
                this.members = null;
        }
    }
}
