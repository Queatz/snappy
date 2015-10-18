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

    public FollowLinkSpec get(String sourceId, String targetId) {
        return Datastore.get(FollowLinkSpec.class).filter("sourceId", sourceId).filter("targetId", targetId).first().now();
    }

    public FollowLinkSpec createOrUpdate(String user, String following) {
        FollowLinkSpec follow = get(user, following);

        if(follow != null) {
            return follow;
        }

        follow = Datastore.create(FollowLinkSpec.class);
        follow.sourceId = Datastore.key(PersonSpec.class, user);
        follow.targetId = Datastore.key(PersonSpec.class, following);

        return follow;
    }
}
