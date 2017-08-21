package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.mines.FollowerMine;
import com.queatz.snappy.logic.mines.MemberMine;
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
     * List of things that this thing is in.
     */
    final List<Viewable> in;

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
        final MemberMine memberMine = use(MemberMine.class);

        switch (view) {
            case DEEP:

                final List<EarthThing> thingMembers = memberMine.byThingWithStatus(thing, Config.MEMBER_STATUS_ACTIVE);

                if (thingMembers != null) {
                    this.members = new EntityListView(as, thingMembers, EarthView.SHALLOW).asList();
                } else {
                    this.members = null;
                }

                final List<EarthThing> thingIsIn = memberMine.isAMemberOfThingsWithStatus(thing, Config.MEMBER_STATUS_ACTIVE);

                if (thingIsIn != null) {
                    this.in = new EntityListView(as, thingIsIn, EarthView.SHALLOW).asList();
                } else {
                    this.in = null;
                }
                break;

            case SHALLOW:

                this.members = null;
                this.in = null;

                break;
            default:
                this.members = null;
                this.in = null;
        }
    }
}
