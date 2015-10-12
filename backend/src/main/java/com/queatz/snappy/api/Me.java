package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.json.JSONException;
import org.json.JSONObject;

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
    public void call() throws IOException, PrintingError {
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

    private void get() throws IOException, PrintingError {
        Document me = Search.getService().get(Search.Type.PERSON, user);

        if (me == null) {
            die("me - inexistent");
        }

        response.getWriter().write(Things.getService().person.toJson(me, user, false).toString());
    }

    private void getBuy() throws IOException, PrintingError {
        Document me = Search.getService().get(Search.Type.PERSON, user);

        if (me == null) {
            die("me - inexistent");
        }

        try {
            String subscription = me.getOnlyField("subscription").getAtom();
            String r;

            if (subscription == null || subscription.isEmpty()) {
                r = Config.HOSTING_ENABLED_FALSE;
            } else if (Config.HOSTING_ENABLED_AVAILABLE.equals(subscription)) {
                r = Config.HOSTING_ENABLED_AVAILABLE;
            } else {
                r = Config.HOSTING_ENABLED_TRUE;
            }

            response.getWriter().write(r);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.getWriter().write(Boolean.toString(false));
        }
    }

    private void post() throws PrintingError {
        Document me = Search.getService().get(Search.Type.PERSON, user);

        if (me == null) {
            die("me - inexistent");
        }

        String about = request.getParameter(Config.PARAM_ABOUT);

        if (about != null) {
            Things.getService().person.updateAbout(me, about);
        }
    }

    private void postUpto() throws IOException, PrintingError {
        Document update = Things.getService().update.createUpto(user);
        GcsFilename photoName = new GcsFilename(api.mAppIdentityService.getDefaultGcsBucketName(), "upto/photo/" + update.getId() + "/" + new Date().getTime());

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
            update = Things.getService().update.setMessage(update, message);
        }

        if (allGood) {
            Push.getService().sendToFollowers(user, Things.getService().update.makePush(update));
        } else {
            die("upto photo - not all good");
        }

        response.getWriter().write(Things.getService().update.toJson(update, user, false).toString());
    }

    private void postOffers() throws IOException, PrintingError {
        String localId = request.getParameter(Config.PARAM_LOCAL_ID);
        String details = request.getParameter(Config.PARAM_DETAILS);

        int price = 0;

        try {
            price = Integer.parseInt(request.getParameter(Config.PARAM_PRICE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (details != null && details.length() > 0) {
            Document offer = Things.getService().offer.create(user, details, price);

            if (offer != null) {
                JSONObject json = Things.getService().offer.toJson(offer, user, false);
                Util.localId(json, localId);

                response.getWriter().write(json.toString());
            } else {
                error("offers - error");
            }
        }
    }

    private void postBuy(String purchaseData) throws IOException, PrintingError {
        response.getWriter().write(Boolean.toString(Buy.getService().validate(user, purchaseData)));
    }

    private void postRegisterDevice(String deviceId, String socialMode) {
        if (deviceId != null && deviceId.length() > 0) {
            Push.getService().register(user, deviceId, socialMode);
        }
    }

    private void postUnregisterDevice(String deviceId) {
        if (deviceId != null && deviceId.length() > 0) {
            Push.getService().unregister(user, deviceId);
        }
    }

    private void postClearNotification(String notification) {
        try {
            JSONObject push = Util.makeSimplePush(Config.PUSH_ACTION_CLEAR_NOTIFICATION);
            push.put("notification", notification);
            Push.getService().send(user, push);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void deleteOffer(String offerId) {
        Document offer = Search.getService().get(Search.Type.OFFER, offerId);

        if (offer != null && user.equals(offer.getOnlyField("person").getAtom())) {
            Things.getService().offer.delete(offer);
        }
    }
}
