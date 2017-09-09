package com.queatz.snappy.logic.interfaces;

import com.google.common.base.Strings;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.EarthVisibility;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.ContactEditor;
import com.queatz.snappy.logic.editors.MemberEditor;
import com.queatz.snappy.logic.eventables.NewThingEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.util.Map;

/**
 * Common thing interface
 *
 * Supports
 *
 * GET /:id             -> thing
 * GET /:id/photo       -> thing photo
 *
 * POST ?kind=...       -> new thing (optional: ?in=12345)
 * POST /:id            -> update thing
 * POST /:id/photo      -> update photo
 *
 * Created by jacob on 5/22/16.
 */
public abstract class CommonThingInterface implements Interfaceable {
    /**
     * Implement this method to create new things of this kind.
     *
     * @return A newly created thing.
     */
    public abstract EarthThing createThing(EarthAs as);

    /**
     * Implement this method to edit things of this kind.
     *
     * Paths match: /:id?custom_params
     *
     * @param thing The thing to edit.
     * @return The edited thing, or null to not write the thing view.
     */
    public abstract EarthThing editThing(EarthAs as, EarthThing thing);

    /**
     * Implement this method to delete things of this kind.
     *
     * @param thing The thing to delete.
     * @return If the thing was deleted.
     */
    public boolean deleteThing(EarthAs as, EarthThing thing) {
        new EarthStore(as).conclude(thing);
        return true;
    }

    /**
     * Implement this method to add custom getters.
     *
     * Paths match: /:id/custom_string
     *
     * @return Any string response, or null to relay no action was taken
     */
    public String getThing(EarthAs as,  EarthThing thing) {
        return null;
    }
    /**
     * Implement this method to add custom setters.
     *
     * Paths match: /:id/custom_string
     *
     * @return Any string response, or null to relay no action was taken
     */
    public String postThing(EarthAs as,  EarthThing thing) {
        return null;
    }

    public void onGet(EarthAs as, EarthThing person) {

    }

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("thing - empty route");
            case 1: {
                EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

                onGet(as, thing);

                return new EarthViewer(as).getViewForEntityOrThrow(thing).toJson();
            } case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_PHOTO:
                        getPhoto(as);
                        return null;
                    default:
                        EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

                        String string = getThing(as, thing);
                        if (string == null) {
                            throw new NothingLogicResponse("thing - bad path");
                        }

                        return string;
                }
            default:
                throw new NothingLogicResponse("thing - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        as.requireUser();

        switch (as.getRoute().size()) {
            case 0: {
                EarthThing thing = this.createThing(as);

                // Add contact
                EarthThing contact = new ContactEditor(as).newContact(thing, as.getUser());
                new MemberEditor(as).create(contact, thing, Config.MEMBER_STATUS_ACTIVE);

                new EarthUpdate(as).send(new NewThingEvent(thing)).toFollowersOf(as.getUser());

                String in = extract(as.getParameters().get(Config.PARAM_IN));

                if (!Strings.isNullOrEmpty(in)) {
                    EarthThing of = new EarthStore(as).get(in);

                    if (of != null) {
                        // TODO: Make suggestion if not owned by me
                        new MemberEditor(as).create(thing, of, Config.MEMBER_STATUS_ACTIVE);
                    } else {
                        // Silent fail
                    }
                }

                setVisibility(as, thing);

                return new EarthViewer(as).getViewForEntityOrThrow(thing).toJson();
            }
            case 1: {
                EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

                thing = this.editThing(as, thing);

                if (thing == null) {
                    return null;
                }

                setVisibility(as, thing);

                return new EarthViewer(as).getViewForEntityOrThrow(thing).toJson();
            }

            case 2: {
                if (Config.PATH_PHOTO.equals(as.getRoute().get(1))) {
                    postPhoto(new EarthStore(as).get(as.getRoute().get(0)), as);
                    return new SuccessView(true).toJson();
                } else if (Config.PATH_DELETE.equals(as.getRoute().get(1))) {
                    EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));
                    return new SuccessView(deleteThing(as, thing)).toJson();
                } else {
                    EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));
                    String string = postThing(as, thing);
                    if (string != null) {
                        return string;
                    }
                }

                throw new NothingLogicResponse("thing - bad path");
            }
            case 3: {
                EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

                if (Config.PATH_PHOTO.equals(as.getRoute().get(1))) {
                    if (Config.PATH_DELETE.equals(as.getRoute().get(2))) {
                        removePhoto(thing, as);
                        return new SuccessView(true).toJson();
                    }
                }

                String string = postThing(as, thing);
                if (string != null) {
                    return string;
                }

                throw new NothingLogicResponse("thing - bad path");
            }
        }

        return null;
    }

    // Common visibility
    protected void setVisibility(EarthAs as, EarthThing thing) {
        String hidden = extract(as.getParameters().get(Config.PARAM_HIDDEN));

        if (hidden != null) {
            setVisibilityHidden(as, thing, hidden);
        }

        String clubs = extract(as.getParameters().get(Config.PARAM_CLUBS));

        if (clubs != null) {
            setVisibilityClubs(as, thing, clubs);
        }
    }

    protected void setVisibilityClubs(EarthAs as, EarthThing thing, String clubs) {
        try {
            Map<String, Boolean> clubsMap = new EarthJson().fromJson(
                    clubs,
                    new TypeToken<Map<String, Boolean>>() {}.getType()
            );

            as.s(EarthVisibility.class).setVisibility(thing, clubsMap);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    protected void setVisibilityHidden(EarthAs as, EarthThing thing, String hidden) {
        as.s(EarthVisibility.class).setHidden(thing, Boolean.parseBoolean(hidden));
    }

    private void getPhoto(EarthAs as) {
        EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

        if (!thing.getBoolean(EarthField.PHOTO)) {
            throw new PrintingError(Api.Error.NOT_FOUND, "thing - photo not set");
        }

        try {
            if(!ApiUtil.getPhoto(thing.key().name(), as.getApi(), as.getRequest(), as.getResponse())) {
                throw new PrintingError(Api.Error.NOT_FOUND, "thing - no photo");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.NOT_FOUND, "thing - photo io error");
        }
    }

    protected EarthThing postPhoto(EarthThing thing, EarthAs as) {
        try {
            boolean photo = ApiUtil.putPhoto(thing.key().name(), as.getApi(), as.getRequest());

            EarthStore earthStore = new EarthStore(as);
            return earthStore.save(earthStore.edit(thing)
                    .set(EarthField.PLACEHOLDER)
                    .set(EarthField.ASPECT_RATIO)
                    .set(EarthField.PHOTO, photo));
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.NOT_FOUND, "thing - photo io error");
        }
    }

    protected EarthThing removePhoto(EarthThing thing, EarthAs as) {
        EarthStore earthStore = new EarthStore(as);
        return earthStore.save(earthStore.edit(thing).set(EarthField.PHOTO, false));
    }

    protected String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }
}
