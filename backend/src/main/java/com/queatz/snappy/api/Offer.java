package com.queatz.snappy.api;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.EndorsementSpec;
import com.queatz.snappy.shared.things.OfferSpec;
import com.queatz.snappy.shared.things.UpdateLikeSpec;
import com.queatz.snappy.shared.things.UpdateSpec;

import java.io.IOException;

/**
 * Created by jacob on 12/26/15.
 */
public class Offer extends Api.Path {

    public Offer(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        switch (method) {
            case GET:
                switch (path.size()) {
                    case 2:
                        if (Config.PATH_ENDORSERS.equals(path.get(1))) {
                            getEndorsers(path.get(0));
                            break;
                        }
                    default:
                        die("offer - bad path");
                }

                break;
            case POST:
                switch (path.size()) {
                    case 2:
                        if (Config.PATH_ENDORSE.equals(path.get(1))) {
                            endorse(path.get(0));
                            break;
                        }
                    default:
                        die("offer - bad path");
                }

                break;
        }
    }

    private void getEndorsers(String offerId) {
        OfferSpec update = Datastore.get(OfferSpec.class, offerId);

        if (update == null) {
            notFound();
        }

        ok(Datastore.get(EndorsementSpec.class).filter("targetId", update).list());
    }

    private void endorse(String offerId) {
        OfferSpec offer = Datastore.get(OfferSpec.class, offerId);

        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        if (offer == null) {
            notFound();
        }

        if (user.id.equals(Datastore.id(offer.personId))) {
            die("offer - you can't endorse yourself");
        }

        EndorsementSpec endorsement = Thing.getService().offer.endorse(offer, user);

        if (endorsement != null) {
            endorsement.localId = localId;

            if (!user.id.equals(Datastore.id(offer.personId))) {
                Push.getService().send(Datastore.id(offer.personId), new PushSpec<>(Config.PUSH_ACTION_OFFER_ENDORSEMENT, endorsement));
            }
        }

        ok(endorsement);
    }
}