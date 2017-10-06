package com.queatz.snappy.logic.interfaces;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthStore;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.view.EarthView;
import com.queatz.snappy.logic.editors.LikeEditor;
import com.queatz.snappy.logic.editors.OfferEditor;
import com.queatz.snappy.logic.eventables.NewOfferEvent;
import com.queatz.snappy.logic.eventables.OfferLikeEvent;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.LikeMine;
import com.vlllage.things.EntityListView;
import com.queatz.snappy.logic.views.LikeView;
import com.queatz.snappy.view.SuccessView;
import com.queatz.snappy.shared.Config;

import java.util.List;

/**
 * Created by jacob on 5/9/16.
 */
public class OfferInterface extends CommonThingInterface {

    @Override
    public String getThing(EarthAs as, EarthThing thing) {
        switch (as.getRoute().size()) {
            case 2:
                if (Config.PATH_LIKERS.equals(as.getRoute().get(1))) {
                    return getLikers(as, as.getRoute().get(0));
                }
            default:
                return null;
        }
    }

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);
        String details = as.getRequest().getParameter(Config.PARAM_DETAILS);
        String unit = as.getRequest().getParameter(Config.PARAM_UNIT);
        boolean want = Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_WANT));

        // @deprecated
        Integer price = null;

        if (as.getRequest().getParameter(Config.PARAM_PRICE) != null) try {
            price = Integer.parseInt(as.getRequest().getParameter(Config.PARAM_PRICE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (details != null && details.length() > 0) {

            // Validate pricing
            if (price != null) {
                price = Math.min(Config.PAID_OFFER_PRICE_MAX, Math.max(Config.PAID_OFFER_PRICE_MIN, price));

                if (Math.abs(price) < 200) {
                    price = (int) Math.floor(price / 10) * 10;
                } else if (Math.abs(price) < 1000) {
                    price = (int) Math.floor(price / 50) * 50;
                } else {
                    price = (int) Math.floor(price / 100) * 100;
                }
            }

            EarthThing offer = new OfferEditor(as).newOffer(as.getUser(), details, want, price, unit);

            if (offer != null) {
                new EarthUpdate(as).send(new NewOfferEvent(offer))
                        .toFollowersOf(as.getUser());

                return offer.setLocalId(localId);
            } else {
                throw new NothingLogicResponse("offers - error");
            }
        }

        return null;
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing thing) {
        as.requireUser();

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
                price = Math.min(Config.PAID_OFFER_PRICE_MAX, Math.max(Config.PAID_OFFER_PRICE_MIN, price));

                if (Math.abs(price) < 200) {
                    price = (int) Math.floor(price / 10) * 10;
                } else if (Math.abs(price) < 1000) {
                    price = (int) Math.floor(price / 50) * 50;
                } else {
                    price = (int) Math.floor(price / 100) * 100;
                }
            }

            thing = new OfferEditor(as).edit(thing, details, price, unit).setLocalId(localId);
        }

        return thing;
    }

    @Override
    public String postThing(EarthAs as, EarthThing thing) {
        as.requireUser();

        switch (as.getRoute().size()) {
            case 0:
                break;
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_LIKE:
                        return like(as, as.getRoute().get(0));
                }

                break;
        }

        throw new NothingLogicResponse("offer - bad path");
    }

    private String getLikers(EarthAs as, String offerId) {
        EarthThing offer = new EarthStore(as).get(offerId);

        List<EarthThing> likers = new EarthStore(as).find(EarthKind.LIKE_KIND, EarthField.TARGET, offer.key());

        return new EntityListView(as, likers, EarthView.SHALLOW).toJson();
    }

    private String like(EarthAs as, String offerId) {
        as.requireUser();

        EarthThing offer = new EarthStore(as).get(offerId);

        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        EarthThing like = new LikeMine(as).getLike(as.getUser(), offer);

        if (like != null) {
            return new SuccessView(false).toJson();
        }

        like = new LikeEditor(as).newLike(as.getUser(), offer);

        new EarthUpdate(as).send(new OfferLikeEvent(like))
                .to(offer.getKey(EarthField.SOURCE));

        return new LikeView(as, like).setLocalId(localId).toJson();
    }
}
