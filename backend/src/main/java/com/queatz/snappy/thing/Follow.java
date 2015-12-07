package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.things.FollowLinkSpec;
import com.queatz.snappy.shared.things.PersonSpec;

/**
 * Created by jacob on 2/19/15.
 */
public class Follow {
    public void stopFollowing(FollowLinkSpec follow) {
        Datastore.delete(follow);
    }

    public FollowLinkSpec get(PersonSpec sourceId, PersonSpec targetId) {
        return Datastore.get(FollowLinkSpec.class).filter("sourceId", sourceId).filter("targetId", targetId).first().now();
    }

    public FollowLinkSpec create(PersonSpec user, PersonSpec following) {
        FollowLinkSpec follow = get(user, following);

        if(follow == null) {
            follow = Datastore.create(FollowLinkSpec.class);
            follow.sourceId = Datastore.key(user);
            follow.targetId = Datastore.key(following);
            Datastore.save(follow);

            return follow;
        }

        return null;
    }
}
