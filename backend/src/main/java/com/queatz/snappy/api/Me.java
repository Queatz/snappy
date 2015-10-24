package com.queatz.snappy.api;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.GooglePurchaseDataSpec;
import com.queatz.snappy.backend.Json;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.OfferSpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.UpdateSpec;

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
import java.util.logging.Logger;

/**
 * Created by jacob on 2/8/15.
 */

public class Me extends Api.Path {
    public Me(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        switch (method) {
            case GET:
                if (path.size() == 0) {
                    get();
                } else if (path.size() == 1) {
                    switch (path.get(0)) {
                        case Config.PATH_BUY:
                            getBuy();

                            break;
                        default:
                            die("me - bad path");
                    }
                } else {
                    die("me - bad path");
                }

                break;
            case POST:
                if (path.size() == 0) {
                    post();

                    break;
                } else if (path.size() == 1) {
                    switch (path.get(0)) {
                        case Config.PATH_UPTO:
                            postUpto();

                            break;
                        case Config.PATH_OFFERS:
                            postOffers();

                            break;
                        case Config.PATH_BUY:
                            postBuy(request.getParameter(Config.PARAM_PURCHASE_DATA));

                            break;
                        case Config.PATH_REGISTER_DEVICE:
                            postRegisterDevice(
                                    request.getParameter(Config.PARAM_DEVICE_ID),
                                    request.getParameter(Config.PARAM_SOCIAL_MODE)
                            );

                            break;
                        case Config.PATH_UNREGISTER_DEVICE:
                            postUnregisterDevice(request.getParameter(Config.PARAM_DEVICE_ID));

                            break;
                        case Config.PATH_CLEAR_NOTIFICATION:
                            postClearNotification(request.getParameter(Config.PARAM_NOTIFICATION));

                            break;
                        default:
                            die("me - bad path");
                    }
                } else {
                    die("me - bad path");
                }

                break;
            case DELETE:
                if (path.size() == 2) {
                    if (path.get(0).equals(Config.PATH_OFFERS)) {
                        deleteOffer(path.get(1));
                    } else {
                        die("me - bad path");
                    }
                } else {
                    die("me - bad path");
                }

                break;
            default:
                die("me - bad method");
        }
    }

    private void get() {
        ok(user);
    }

    private void getBuy() {
        PersonSpec me = user;

        if (me == null) {
            die("me - inexistent");
        }

        String r;

        if (StringUtils.isBlank(me.subscription)) {
            r = Config.HOSTING_ENABLED_FALSE;
        } else if (Config.HOSTING_ENABLED_AVAILABLE.equals(me.subscription)) {
            r = Config.HOSTING_ENABLED_AVAILABLE;
        } else {
            r = Config.HOSTING_ENABLED_TRUE;
        }

        ok(r);
    }

    private void post() {
        PersonSpec me = user;

        if (me == null) {
            die("me - inexistent");
        }

        String about = request.getParameter(Config.PARAM_ABOUT);

        if (about != null) {
            Thing.getService().person.updateAbout(me, about);
        }
    }

    private void postUpto() throws IOException {
        UpdateSpec update = Thing.getService().update.createUpto(user.id);
        GcsFilename photoName = new GcsFilename(api.mAppIdentityService.getDefaultGcsBucketName(), "upto/photo/" + update.id + "/" + new Date().getTime());

        String message = null;
        boolean allGood = false;

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(request);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    int len;
                    byte[] buffer = new byte[8192];

                    GcsOutputChannel outputChannel = api.mGCS.createOrReplace(photoName, GcsFileOptions.getDefaultInstance());

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
        catch (FileUploadException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
            error("upto photo - couldn't upload because " + e);
        }

        if (message != null) {
            update = Thing.getService().update.setMessage(update, message);
        }

        if (allGood) {
            Push.getService().sendToFollowers(user.id, new PushSpec<>(Config.PUSH_ACTION_NEW_UPTO, update));
        } else {
            die("upto photo - not all good");
        }

        ok(update);
    }

    private void postOffers() {
        String localId = request.getParameter(Config.PARAM_LOCAL_ID);
        String details = request.getParameter(Config.PARAM_DETAILS);

        int price = 0;

        try {
            price = Integer.parseInt(request.getParameter(Config.PARAM_PRICE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (details != null && details.length() > 0) {
            OfferSpec offer = Thing.getService().offer.create(user, details, price);

            if (offer != null) {
                offer.localId = localId;
                ok(offer);
            } else {
                error("offers - error");
            }
        }
    }

    private void postBuy(String purchaseData) {
        ok(Buy.getService().validate(user, Json.from(purchaseData, GooglePurchaseDataSpec.class)));
    }

    private void postRegisterDevice(String deviceId, String socialMode) {
        if (deviceId != null && deviceId.length() > 0) {
            Push.getService().register(user.id, deviceId, socialMode);
        }
    }

    private void postUnregisterDevice(String deviceId) {
        if (deviceId != null && deviceId.length() > 0) {
            Push.getService().unregister(user.id, deviceId);
        }
    }

    private void postClearNotification(final String n) {
        Push.getService().send(user.id, new PushSpec<>(
                Config.PUSH_ACTION_CLEAR_NOTIFICATION,
                ImmutableMap.of("notification", n)
        ));
    }

    private void deleteOffer(String offerId) {
        OfferSpec offer = Datastore.get(OfferSpec.class, offerId);

        if (offer != null && user.id.equals(Datastore.id(offer.personId))) {
            Thing.getService().offer.delete(offer.id);
        }
    }
}
