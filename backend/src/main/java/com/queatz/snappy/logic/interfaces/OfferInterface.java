package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.LikeEditor;
import com.queatz.snappy.logic.editors.OfferEditor;
import com.queatz.snappy.logic.eventables.OfferLikeEvent;
import com.queatz.snappy.logic.exceptions.LogicException;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.logic.views.LikeView;
import com.queatz.snappy.logic.views.OfferView;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 5/9/16.
 */
public class OfferInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                if (Config.PATH_LIKERS.equals(as.getRoute().get(1))) {
                    return getLikers(as, as.getRoute().get(0));
                } else if (Config.PATH_PHOTO.equals(as.getRoute().get(1))) {
                    return getPhoto(as, as.getRoute().get(0));
                }
            default:
                throw new NothingLogicResponse("offer - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                break;
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_DELETE:
                        new EarthStore(as).conclude(as.getRoute().get(0));
                        return new SuccessView(true).toJson();
                    case Config.PATH_LIKE:
                        return like(as, as.getRoute().get(0));
                    case Config.PATH_PHOTO:
                        return addPhoto(as, as.getRoute().get(0));
                    case Config.PATH_EDIT:
                        return edit(as, as.getRoute().get(0));
                }

                break;
            case 3:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_PHOTO:
                        switch (as.getRoute().get(2)) {
                            case Config.PATH_DELETE:
                                return deletePhoto(as, as.getRoute().get(0));
                        }
                        break;
                }

                break;
        }

        throw new NothingLogicResponse("offer - bad path");
    }


    private String addPhoto(EarthAs as, String offerId) {
        Entity offer = new EarthStore(as).get(offerId);

        try {
            if (!ApiUtil.putPhoto("offer/photo/" + offer.key().name() + "/" + new Date().getTime(), as.getApi(), as.getRequest())) {
                throw new LogicException("offer photo - not all good");
            }
        } catch (IOException e) {
            throw new LogicException("offer photo - not all good");
        }

        offer = new OfferEditor(as).setPhoto(offer, true);

        return new EarthViewer(as).getViewForEntityOrThrow(offer).toJson();
    }

    private String getPhoto(EarthAs as, String offerId) {
        try {
            if (!ApiUtil.getPhoto("offer/photo/" + offerId + "/", as.getApi(), as.getRequest(), as.getResponse())) {
                throw new NothingLogicResponse("offer photo - not found");
            }
        } catch (IOException e) {
            throw new LogicException("offer photo - not all good");
        }

        return null;
    }

    private String edit(EarthAs as, String offerId) {
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);
        String details = as.getRequest().getParameter(Config.PARAM_DETAILS);
        String unit = as.getRequest().getParameter(Config.PARAM_UNIT);

        Integer price = null;

        if (as.getRequest().getParameter(Config.PARAM_PRICE) != null) try {
            price = Integer.parseInt(as.getRequest().getParameter(Config.PARAM_PRICE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (details != null && details.length() > 0) {

            // Validate pricing
            if (price != null) {
                if (new Buy(as).valid(as.getUser())) {
                    price = Math.min(Config.PAID_OFFER_PRICE_MAX, Math.max(Config.PAID_OFFER_PRICE_MIN, price));
                } else {
                    price = Math.min(Config.FREE_OFFER_PRICE_MAX, Math.max(Config.FREE_OFFER_PRICE_MIN, price));
                }

                if (Math.abs(price) < 200) {
                    price = (int) Math.floor(price / 10) * 10;
                } else if (Math.abs(price) < 1000) {
                    price = (int) Math.floor(price / 50) * 50;
                } else {
                    price = (int) Math.floor(price / 100) * 100;
                }
            }

            Entity offer = new OfferEditor(as).edit(new EarthStore(as).get(offerId), details, price, unit);

            return new OfferView(as, offer).setLocalId(localId).toJson();
        }

        return new SuccessView(false).toJson();
    }

    private String deletePhoto(EarthAs as, String offerId) {
        new OfferEditor(as).setPhoto(new EarthStore(as).get(offerId), false);
        return new SuccessView(true).toJson();
    }

    private String getLikers(EarthAs as, String offerId) {
        Entity offer = new EarthStore(as).get(offerId);

        List<Entity> likers = new EarthStore(as).find(EarthKind.LIKE_KIND, EarthField.TARGET, offer.key());

        return new EntityListView(as, likers).toJson();
    }

    private String like(EarthAs as, String offerId) {
        Entity offer = new EarthStore(as).get(offerId);

        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        Entity like = new LikeEditor(as).newLike(offer, as.getUser());

        new EarthUpdate(as).send(new OfferLikeEvent(like))
                .to(offer.getKey(EarthField.SOURCE));

        return new LikeView(as, like).setLocalId(localId).toJson();
    }
}
