package com.queatz.snappy.api;

import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.EndorsementSpec;
import com.queatz.snappy.shared.things.LocationSpec;
import com.queatz.snappy.shared.things.OfferSpec;

import java.io.IOException;
import java.util.Date;

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
                        } else if (Config.PATH_PHOTO.equals(path.get(1))) {
                            getPhoto(path.get(0));
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
                        } else {
                            if (Config.PATH_PHOTO.equals(path.get(1))) {
                                addPhoto(path.get(0));
                                break;
                            }
                        }
                    default:
                        die("offer - bad path");
                }

                break;
            case DELETE:
                switch (path.size()) {
                    case 2:
                        if (Config.PATH_PHOTO.equals(path.get(1))) {
                            deletePhoto(path.get(0));
                            break;
                        }
                    default:
                        die("offer - bad path");
                }

                break;
        }
    }

    private void addPhoto(String offerId) throws IOException {
        OfferSpec offer = Datastore.get(OfferSpec.class, offerId);

        if (offer == null) {
            notFound();
        }

        if (!ApiUtil.putPhoto("offer/photo/" + offer.id + "/" + new Date().getTime(), api, request)) {
            die("offer photo - not all good");
        }

        Thing.getService().offer.addPhoto(offerId);

        ok(true);
    }

    private void getPhoto(String offerId) throws IOException {
        if (!ApiUtil.getPhoto("offer/photo/" + offerId + "/", api, request, response)) {
            notFound();
        }
    }

    private void deletePhoto(String offerId) {
        Thing.getService().offer.deletePhoto(offerId);
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