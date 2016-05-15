package com.queatz.snappy.logic.interfaces;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.StructuredQuery;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.queatz.snappy.backend.GooglePurchaseDataSpec;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.OfferEditor;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.editors.UpdateEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.MessagesAndContactsView;
import com.queatz.snappy.logic.views.OfferView;
import com.queatz.snappy.logic.views.PersonView;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.logic.views.UpdateView;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jacob on 5/14/16.
 */
public class MeInterface implements Interfaceable {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    PersonEditor personEditor = EarthSingleton.of(PersonEditor.class);
    UpdateEditor updateEditor = EarthSingleton.of(UpdateEditor.class);
    OfferEditor offerEditor = EarthSingleton.of(OfferEditor.class);
    EarthJson earthJson = EarthSingleton.of(EarthJson.class);

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                return new PersonView(as.getUser()).toJson();
            case 1:
                switch (as.getRoute().get(0)) {
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

    private String getMessages(EarthAs as) {
        // XXX TODO when Datastore supports OR expressions, combine these
        List<Entity> messagesToMe = Lists.newArrayList(
                earthStore.queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, as.getUser().key())
                )
        );

        List<Entity> messagesFromMe = Lists.newArrayList(
                earthStore.queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.TARGET, as.getUser().key())
                )
        );

        List<Entity> messages = Lists.newArrayList();
        messages.addAll(messagesToMe);
        messages.addAll(messagesFromMe);

        List<Entity> contacts = Lists.newArrayList(earthStore.query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.CONTACT_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, as.getUser().key())
        ));

        return new MessagesAndContactsView(messages, contacts).toJson();
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                String about = as.getRequest().getParameter(Config.PARAM_ABOUT);

                if (about != null) {
                    personEditor.updateAbout(as.getUser(), about);
                }

                return new SuccessView(true).toJson();
            case 1:
                switch (as.getRoute().get(0)) {
                    case Config.PATH_UPTO:
                        return postUpdate(as);
                    case Config.PATH_OFFERS:
                        return postOffers(as);
                    case Config.PATH_BUY:
                        return postBuy(as, as.getRequest().getParameter(Config.PARAM_PURCHASE_DATA));
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
            default:
                throw new NothingLogicResponse("me - bad path");
        }
    }

    private String postClearNotification(EarthAs as, String notification) {
        Push.getService().send(as.getUser().key().name(), new PushSpec<>(
                Config.PUSH_ACTION_CLEAR_NOTIFICATION,
                ImmutableMap.of("notification", notification)
        ));

        return new SuccessView(true).toJson();
    }

    private String postUnregisterDevice(EarthAs as, String deviceId) {
        if (deviceId != null && deviceId.length() > 0) {
            Push.getService().unregister(as.getUser().key().name(), deviceId);
            return new SuccessView(true).toJson();
        } else {
            return new SuccessView(false).toJson();
        }
    }

    private String postRegisterDevice(EarthAs as, String deviceId, String socialMode) {
        if (deviceId != null && deviceId.length() > 0) {
            Push.getService().register(as.getUser().key().name(), deviceId, socialMode);
            return new SuccessView(true).toJson();
        } else {
            return new SuccessView(false).toJson();
        }
    }

    private String postBuy(EarthAs as, String purchaseData) {
        boolean ok = Buy.getService().validate(as.getUser(), earthJson.fromJson(purchaseData, GooglePurchaseDataSpec.class));

        return new SuccessView(ok).toJson();
    }

    private String postOffers(EarthAs as) {
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
                if (Buy.getService().valid(as.getUser())) {
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

            Entity offer = offerEditor.newOffer(as.getUser(), details, price, unit);

            if (offer != null) {
                Push.getService().sendToFollowers(as.getUser().key().name(), new PushSpec<>(Config.PUSH_ACTION_NEW_OFFER, offer));

                return new OfferView(offer).setLocalId(localId).toJson();
            } else {
                throw new NothingLogicResponse("offers - error");
            }
        }

        return null;
    }

    private String postUpdate(EarthAs as) {
        Entity update = updateEditor.newUpdate(as.getUser());
        GcsFilename photoName = new GcsFilename(as.getApi().mAppIdentityService.getDefaultGcsBucketName(), "upto/photo/" + update.key().name() + "/" + new Date().getTime());

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

                    GcsOutputChannel outputChannel = as.getApi().mGCS.createOrReplace(photoName, GcsFileOptions.getDefaultInstance());

                    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                        outputChannel.write(ByteBuffer.wrap(buffer, 0, len));
                    }

                    outputChannel.close();

                    allGood = true;

                    break;
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
            update = updateEditor.setMessage(update, message);
        }

        if (allGood) {
            Push.getService().sendToFollowers(as.getUser().key().name(), new PushSpec<>(Config.PUSH_ACTION_NEW_UPTO, update));
        } else {
            throw new NothingLogicResponse("upto photo - not all good");
        }

        return new UpdateView(update).toJson();
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