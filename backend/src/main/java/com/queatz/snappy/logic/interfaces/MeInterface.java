package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.backend.GooglePurchaseDataSpec;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthEmail;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.DeviceEditor;
import com.queatz.snappy.logic.editors.OfferEditor;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.editors.UpdateEditor;
import com.queatz.snappy.logic.eventables.ClearNotificationEvent;
import com.queatz.snappy.logic.eventables.NewOfferEvent;
import com.queatz.snappy.logic.eventables.NewUpdateEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.MessageMine;
import com.queatz.snappy.logic.mines.RecentMine;
import com.queatz.snappy.logic.views.MessagesAndContactsView;
import com.queatz.snappy.logic.views.OfferView;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.shared.Config;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jacob on 5/14/16.
 */
public class MeInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                return new EarthViewer(as).getViewForEntityOrThrow(as.getUser(), EarthView.SHALLOW).toJson();
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_BUY:
                        return getBuy(as);
                    case Config.PATH_MESSAGES:
                        return getMessages(as);
                }
                // Fall-through
            default:
                throw new NothingLogicResponse("me - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                String about = as.getRequest().getParameter(Config.PARAM_ABOUT);

                if (about != null) {
                    new PersonEditor(as).updateAbout(as.getUser(), about);
                }

                return new SuccessView(true).toJson();
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_UPTO:
                        return postUpdate(as);
                    case Config.PATH_OFFERS:
                        return postOffers(as);
                    case Config.PATH_BUY:
                        return postBuy(as, as.getRequest().getParameter(Config.PARAM_PURCHASE_DATA));
                    case Config.PATH_INFO:
                        return postInfo(as);
                    case Config.PATH_REGISTER_DEVICE:
                        return postRegisterDevice(as,
                                as.getRequest().getParameter(Config.PARAM_DEVICE_ID),
                                as.getRequest().getParameter(Config.PARAM_SOCIAL_MODE)
                        );
                    case Config.PATH_UNREGISTER_DEVICE:
                        return postUnregisterDevice(as, as.getRequest().getParameter(Config.PARAM_DEVICE_ID));
                    case Config.PATH_CLEAR_NOTIFICATION:
                        return postClearNotification(as, as.getRequest().getParameter(Config.PARAM_NOTIFICATION));
                    default:
                        throw new NothingLogicResponse("me - bad path");
                }
            case 3:
                switch (as.getRoute().get(1)){
                    case Config.PATH_REPORT:
                        return postReport(as, as.getRoute().get(2));
                    default:
                        throw new NothingLogicResponse("me - bad path");
                }
            default:
                throw new NothingLogicResponse("me - bad path");
        }
    }

    private String postInfo(EarthAs as) {
        String latitudeParam = as.getParameters().get(Config.PARAM_LATITUDE)[0];
        String longitudeParam = as.getParameters().get(Config.PARAM_LONGITUDE)[0];
        double latitude = Double.valueOf(latitudeParam);
        double longitude = Double.valueOf(longitudeParam);
        final EarthGeo latLng = EarthGeo.of(latitude, longitude);

        new PersonEditor(as).updateLocation(as.getUser(), latLng);

        return new SuccessView(true).toJson();
    }

    private String postReport(EarthAs as, String personId) {
        EarthThing jacob = new EarthStore(as).get("-697803823443327660");
        EarthThing person = new EarthStore(as).get(personId);
        String feedback = as.getRequest().getParameter(Config.PARAM_MESSAGE);

        String report = "reported person with email " +
                person.getString(EarthField.EMAIL) +
                " with the following message:<br /><br />";

        new EarthEmail().sendRawEmail(as.getUser(), jacob, "Village person reported", report + feedback);

        return new SuccessView(true).toJson();
    }

    private String getMessages(EarthAs as) {
        // XXX TODO when Datastore supports OR expressions, combine these
        List<EarthThing> messagesToMe = new MessageMine(as).messagesFrom(as.getUser().key());
        List<EarthThing> messagesFromMe = new MessageMine(as).messagesTo(as.getUser().key());

        List<EarthThing> messages = new ArrayList<>();
        messages.addAll(messagesToMe);
        messages.addAll(messagesFromMe);

        List<EarthThing> contacts = new RecentMine(as).forPerson(as.getUser());

        return new MessagesAndContactsView(as, messages, contacts).toJson();
    }

    private String postClearNotification(EarthAs as, String notification) {
        new EarthUpdate(as).send(new ClearNotificationEvent(notification))
                .to(as.getUser());

        return new SuccessView(true).toJson();
    }

    private String postUnregisterDevice(EarthAs as, String deviceId) {
        if (deviceId != null && deviceId.length() > 0) {
            new DeviceEditor(as).removeFor(as.getUser().key().name(), deviceId);
            return new SuccessView(true).toJson();
        } else {
            return new SuccessView(false).toJson();
        }
    }

    private String postRegisterDevice(EarthAs as, String deviceId, String socialMode) {
        if (deviceId != null && deviceId.length() > 0) {
            new DeviceEditor(as).newDevice(as.getUser().key().name(), deviceId, socialMode);
            return new SuccessView(true).toJson();
        } else {
            return new SuccessView(false).toJson();
        }
    }

    private String postBuy(EarthAs as, String purchaseData) {
        boolean ok = new Buy(as).validate(as.getUser(), new EarthJson().fromJson(purchaseData, GooglePurchaseDataSpec.class));

        return new SuccessView(ok).toJson();
    }

    /**
     * @deprecated
     * Use OfferInterface
     */
    private String postOffers(EarthAs as) {
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

            EarthThing offer = new OfferEditor(as).newOffer(as.getUser(), details, want, price, unit);

            if (offer != null) {
                new EarthUpdate(as).send(new NewOfferEvent(offer))
                        .toFollowersOf(as.getUser());

                return new OfferView(as, offer).setLocalId(localId).toJson();
            } else {
                throw new NothingLogicResponse("offers - error");
            }
        }

        return null;
    }

    /**
     * @deprecated Use UpdateInterface
     */
    private String postUpdate(EarthAs as) {
        EarthThing update = new UpdateEditor(as).newUpdate(as.getUser());
        String photoName = "earth/thing/photo/" + update.key().name();

        String message = null;
        boolean allGood = false;

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(as.getRequest());
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    int len;
                    byte[] buffer = new byte[8192];

                    OutputStream outputChannel = as.getApi().snappyImage.openOutputStream(photoName);

                    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                        outputChannel.write(buffer, 0, len);
                    }

                    outputChannel.close();

                    allGood = true;
                }
                else if (Config.PARAM_MESSAGE.equals(item.getFieldName())) {
                    message = Streams.asString(stream, "UTF-8");
                }
            }
        }
        catch (FileUploadException | IOException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("upto photo - couldn't upload because: " + e);
        }

        if (message != null) {
            update = new UpdateEditor(as).setMessage(update, message);
        }

        if (allGood) {
            new EarthUpdate(as).send(new NewUpdateEvent(update))
                    .toFollowersOf(update.getKey(EarthField.TARGET));
        } else {
            throw new NothingLogicResponse("upto photo - not all good");
        }

        return new EarthViewer(as).getViewForEntityOrThrow(update).toJson();
    }

    private String getBuy(EarthAs as) {
        String r;

        if (StringUtils.isBlank(as.getUser().getString(EarthField.SUBSCRIPTION))) {
            r = Config.HOSTING_ENABLED_FALSE;
        } else if (Config.HOSTING_ENABLED_AVAILABLE.equals(as.getUser().getString(EarthField.SUBSCRIPTION))) {
            r = Config.HOSTING_ENABLED_AVAILABLE;
        } else {
            r = Config.HOSTING_ENABLED_TRUE;
        }

        return r;
    }
}
