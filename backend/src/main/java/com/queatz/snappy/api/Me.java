package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.thing.Offer;
import com.queatz.snappy.thing.Thing;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/8/15.
 */

public class Me implements Api.Path {
    Api api;

    public Me(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                if(path.size() == 0) {
                    Document me = Search.getService().get(Search.Type.PERSON, user);

                    if (me == null) {
                        throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - inexistent");
                    }

                    resp.getWriter().write(Things.getService().person.toJson(me, user, false).toString());
                }
                else if(path.size() == 1) {
                    if(Config.PATH_BUY.equals(path.get(0))) {
                        Document me = Search.getService().get(Search.Type.PERSON, user);

                        if (me == null) {
                            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - inexistent");
                        }

                        try {
                            String subscription = me.getOnlyField("subscription").getAtom();
                            String r;

                            if(subscription == null || subscription.isEmpty()) {
                                r = Config.HOSTING_ENABLED_FALSE;
                            }
                            else if(Config.HOSTING_ENABLED_AVAILABLE.equals(subscription)) {
                                r = Config.HOSTING_ENABLED_AVAILABLE;
                            }
                            else {
                                r = Config.HOSTING_ENABLED_TRUE;
                            }

                            resp.getWriter().write(r);
                        }
                        catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            resp.getWriter().write(Boolean.toString(false));
                        }
                    }
                    else {
                        throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - bad path");
                    }
                }
                else {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - bad path");
                }

                break;
            case PUT:

                break;
            case POST:
                if(path.size() == 0) {
                    Document me = Search.getService().get(Search.Type.PERSON, user);

                    if (me == null) {
                        throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - inexistent");
                    }

                    String about = req.getParameter(Config.PARAM_ABOUT);

                    if(about != null) {
                        Things.getService().person.updateAbout(me, about);
                    }

                    break;
                }
                else if(path.size() == 1) {
                    if (Config.PATH_OFFERS.equals(path.get(0))) {
                        String localId = req.getParameter(Config.PARAM_LOCAL_ID);
                        String details = req.getParameter(Config.PARAM_DETAILS);
                        int price = 0;

                        try {
                            price = Integer.parseInt(req.getParameter(Config.PARAM_PRICE));
                        }
                        catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        if (details != null && details.length() > 0) {
                            Document offer = Things.getService().offer.create(user, details, price);

                            if(offer != null) {
                                JSONObject response = Things.getService().offer.toJson(offer, user, false);
                                Util.localId(response, localId);

                                resp.getWriter().write(response.toString());
                            }
                            else {
                                throw new PrintingError(Api.Error.SERVER_ERROR, "offers - error");
                            }
                        }
                    }
                    else if (Config.PATH_BUY.equals(path.get(0))) {
                        resp.getWriter().write(Boolean.toString(Buy.getService().validate(user, req.getParameter(Config.PARAM_PURCHASE_DATA))));
                    } else if (Config.PATH_REGISTER_DEVICE.equals(path.get(0))) {
                        String deviceId = req.getParameter(Config.PARAM_DEVICE_ID);
                        String socialMode = req.getParameter(Config.PARAM_SOCIAL_MODE);

                        if (deviceId != null && deviceId.length() > 0) {
                            Push.getService().register(user, deviceId, socialMode);
                        }
                    } else if (Config.PATH_UNREGISTER_DEVICE.equals(path.get(0))) {
                        String deviceId = req.getParameter(Config.PARAM_DEVICE_ID);

                        if (deviceId != null && deviceId.length() > 0) {
                            Push.getService().unregister(user, deviceId);
                        }
                    } else if (Config.PATH_CLEAR_NOTIFICATION.equals(path.get(0))) {
                        String notification = req.getParameter(Config.PARAM_NOTIFICATION);

                        try {
                            JSONObject push = Util.makeSimplePush(Config.PUSH_ACTION_CLEAR_NOTIFICATION);
                            push.put("notification", notification);
                            Push.getService().send(user, push);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - bad path");
                    }
                }
                else {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - bad path");
                }

                break;
            case DELETE:
                if(path.size() == 2) {
                    if (path.get(0).equals(Config.PATH_OFFERS)) {
                        String offerId = path.get(1);

                        Document offer = Search.getService().get(Search.Type.OFFER, offerId);

                        if(offer != null && user.equals(offer.getOnlyField("person").getAtom())) {
                            Things.getService().offer.delete(offer);
                        }
                    }
                }

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - bad method");
        }
    }
}
