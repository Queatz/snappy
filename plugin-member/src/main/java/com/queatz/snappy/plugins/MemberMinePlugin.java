package com.queatz.snappy.plugins;

import com.queatz.earth.EarthThing;

import java.util.List;

/**
 * Created by jacob on 10/6/17.
 */

public interface MemberMinePlugin {
    List<EarthThing> byThingWithStatus(EarthThing thing, String status);
    List<EarthThing> isAMemberOfThingsWithStatus(EarthThing thing, String status);
}
