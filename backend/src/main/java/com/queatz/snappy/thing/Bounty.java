package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.BountySpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.util.Date;

/**
 * Created by jacob on 9/5/15.
 */
public class Bounty {
    public boolean claim(PersonSpec user, String bountyId) {
        BountySpec bounty = Datastore.get(BountySpec.class, bountyId);

        if(bounty == null || !Config.BOUNTY_STATUS_OPEN.equals(bounty.status)) {
            return false;
        }

        bounty.status = Config.BOUNTY_STATUS_CLAIMED;
        bounty.peopleId = Datastore.key(user);

        return Datastore.save(bounty);
    }

    public boolean finish(PersonSpec user, String bountyId) {
        BountySpec bounty = Datastore.get(BountySpec.class, bountyId);

        if(bounty == null || !Config.BOUNTY_STATUS_CLAIMED.equals(bounty.status)) {
            return false;
        }

        if(!user.id.equals(Datastore.id(bounty.peopleId))) {
            return false;
        }

        bounty.status = Config.BOUNTY_STATUS_FINISHED;

        return Datastore.save(bounty);
    }

    public BountySpec create(PersonSpec user, String details, int price) {
        if(price < Config.BOUNTY_MIN_PRICE  || price > Config.BOUNTY_MAX_PRICE)
            return null;

        BountySpec bounty = new BountySpec();

        bounty.details = details;
        bounty.status = Config.BOUNTY_STATUS_OPEN;
        bounty.price = price;
        bounty.posterId = Datastore.key(user);
        bounty.posted = new Date();
        bounty.latlng = user.latlng;

        Datastore.save(bounty);

        return bounty;
    }

    public boolean delete(BountySpec bounty) {
        if(bounty == null || !Config.BOUNTY_STATUS_OPEN.equals(bounty.status)) {
            return false;
        }

        Datastore.delete(bounty);
        return true;
    }
}
