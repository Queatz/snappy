package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.ContactView;
import com.queatz.snappy.logic.views.EndorsementView;
import com.queatz.snappy.logic.views.FollowerView;
import com.queatz.snappy.logic.views.HubView;
import com.queatz.snappy.logic.views.JoinView;
import com.queatz.snappy.logic.views.LikeView;
import com.queatz.snappy.logic.views.LocationView;
import com.queatz.snappy.logic.views.MessageView;
import com.queatz.snappy.logic.views.OfferView;
import com.queatz.snappy.logic.views.PartyView;
import com.queatz.snappy.logic.views.PersonView;
import com.queatz.snappy.logic.views.RecentView;
import com.queatz.snappy.logic.views.UpdateView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 4/9/16.
 */
public class EarthViewer {
    private static final Map<String, Constructor<? extends Viewable>> mapping = new HashMap<>();

    static {
        try {
            // This is the entity to view mapping!
            mapping.put(EarthKind.HUB_KIND, HubView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.CONTACT_KIND, ContactView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.FOLLOWER_KIND, FollowerView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.LIKE_KIND, LikeView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.OFFER_KIND, OfferView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.MESSAGE_KIND, MessageView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.PERSON_KIND, PersonView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.PARTY_KIND, PartyView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.LOCATION_KIND, LocationView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.RECENT_KIND, RecentView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.ENDORSEMENT_KIND, EndorsementView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.UPDATE_KIND, UpdateView.class.getConstructor(Entity.class));
            mapping.put(EarthKind.JOIN_KIND, JoinView.class.getConstructor(Entity.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public Viewable getViewForEntityOrThrow(Entity entity) {
        Constructor<? extends Viewable> constructor = mapping.get(entity.getString(EarthField.KIND));

        if (constructor == null) {
            throw new NothingLogicResponse("earth viewer - kind does not support viewing: "
                    + entity.getString(EarthField.KIND));
        }

        try {
            return constructor.newInstance(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        throw new NothingLogicResponse("earth viewer - failed to create view for kind: "
                + entity.getString(EarthField.KIND));
    }
}
