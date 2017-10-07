package com.queatz.snappy.plugins;

import com.queatz.earth.EarthThing;

public interface FollowerMinePlugin {
    EarthThing getFollower(EarthThing person, EarthThing isFollowingPerson);
    int countFollowers(EarthThing entity);
    int countFollowing(EarthThing entity);
}
