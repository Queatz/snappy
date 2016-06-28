package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.ContactEditor;
import com.queatz.snappy.logic.eventables.NewThingEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.util.Date;

/**
 * Common thing interface
 *
 * Supports
 *
 * GET /:id             -> thing
 * GET /:id/photo       -> thing photo
 *
 * POST ?kind=...       -> new thing
 * POST /:id            -> update thing
 * POST /:id/photo      -> update photo
 *
 * Created by jacob on 5/22/16.
 */
public abstract class CommonThingInterface implements Interfaceable {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    private final ContactEditor contactEditor = EarthSingleton.of(ContactEditor.class);
    private final EarthViewer earthViewer = EarthSingleton.of(EarthViewer.class);
    private final EarthUpdate earthUpdate = EarthSingleton.of(EarthUpdate.class);

    /**
     * Implement this method to create new things of this kind.
     *
     * @return A newly created thing.
     */
    public abstract Entity createThing(EarthAs as);

    /**
     * Implement this method to edit things of this kind.
     *
     * @param thing The thing to edit.
     * @return The edited thing.
     */
    public abstract Entity editThing(EarthAs as, Entity thing);

    /**
     * Implement this method to delete things of this kind.
     *
     * @param thing The thing to delete.
     * @return If the thing was deleted.
     */
    public boolean deleteThing(EarthAs as, Entity thing) {
        earthStore.conclude(thing);
        return true;
    }

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("thing - empty route");
            case 1:
                Entity thing = earthStore.get(as.getRoute().get(0));

                return earthViewer.getViewForEntityOrThrow(thing).toJson();
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_PHOTO:
                        getPhoto(as);
                        return null;
                    default:
                        throw new NothingLogicResponse("thing - bad path");
                }
            default:
                throw new NothingLogicResponse("thing - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0: {
                Entity thing = this.createThing(as);
                contactEditor.newContact(thing, as.getUser());

                earthUpdate.send(new NewThingEvent(thing)).toFollowersOf(as.getUser());

                return earthViewer.getViewForEntityOrThrow(thing).toJson();
            }
            case 1: {
                Entity thing = earthStore.get(as.getRoute().get(0));

                this.editThing(as, thing);

                return earthViewer.getViewForEntityOrThrow(thing).toJson();
            }

            case 2: {
                if (Config.PATH_PHOTO.equals(as.getRoute().get(1))) {
                    postPhoto(as);
                    return new SuccessView(true).toJson();
                } else if (Config.PATH_DELETE.equals(as.getRoute().get(1))) {
                    Entity thing = earthStore.get(as.getRoute().get(0));
                    return new SuccessView(deleteThing(as, thing)).toJson();
                }

                throw new NothingLogicResponse("thing - bad path");
            }
        }

        return null;
    }

    private void getPhoto(EarthAs as) {
        Entity thing = earthStore.get(as.getRoute().get(0));

        if (!thing.getBoolean(EarthField.PHOTO)) {
            throw new PrintingError(Api.Error.NOT_FOUND, "thing - photo not set");
        }

        try {
            if(!ApiUtil.getPhoto("earth/thing/photo/" + thing.key().name() + "/", as.getApi(), as.getRequest(), as.getResponse())) {
                throw new PrintingError(Api.Error.NOT_FOUND, "thing - no photo");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.NOT_FOUND, "thing - photo io error");
        }
    }

    private void postPhoto(EarthAs as) {
        Entity thing = earthStore.get(as.getRoute().get(0));

        try {
            boolean photo = ApiUtil.putPhoto("earth/thing/photo/" + thing.key().name() + "/" + new Date().getTime(), as.getApi(), as.getRequest());

            earthStore.save(earthStore.edit(thing).set(EarthField.PHOTO, photo));
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.NOT_FOUND, "thing - photo io error");
        }
    }
}
