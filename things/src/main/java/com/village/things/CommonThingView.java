package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.plugins.FollowerMinePlugin;
import com.queatz.snappy.plugins.MemberMinePlugin;
import com.queatz.snappy.view.EarthView;
import com.queatz.snappy.view.Viewable;
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

        backers = use(FollowerMinePlugin.class).countFollowers(thing);
        final MemberMinePlugin memberMine = use(MemberMinePlugin.class);

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
