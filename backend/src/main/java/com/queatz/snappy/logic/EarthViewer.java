package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.ContactView;
import com.queatz.snappy.logic.views.FollowerView;
import com.queatz.snappy.logic.views.HubView;
import com.queatz.snappy.logic.views.JoinView;
import com.queatz.snappy.logic.views.LikeView;
import com.queatz.snappy.logic.views.LocationView;
import com.queatz.snappy.logic.views.MessageView;
import com.queatz.snappy.logic.views.OfferView;
import com.queatz.snappy.logic.views.PartyView;
import com.queatz.snappy.logic.views.PersonView;
import com.queatz.snappy.logic.views.ProjectView;
import com.queatz.snappy.logic.views.RecentView;
import com.queatz.snappy.logic.views.ResourceView;
import com.queatz.snappy.logic.views.UpdateView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 4/9/16.
 */
public class EarthViewer extends EarthControl {
    public EarthViewer(final EarthAs as) {
        super(as);
    }

    private static final Map<String, Constructor<? extends Viewable>> mapping = new HashMap<>();

    private static <T> Constructor<T> getConstructor(Class<T> clazz) {
        try {
            return clazz.getConstructor(EarthAs.class, Entity.class, EarthView.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    static {
        // This is the entity to view mapping!
        mapping.put(EarthKind.HUB_KIND, getConstructor(HubView.class));
        mapping.put(EarthKind.CONTACT_KIND, getConstructor(ContactView.class));
        mapping.put(EarthKind.FOLLOWER_KIND, getConstructor(FollowerView.class));
        mapping.put(EarthKind.LIKE_KIND, getConstructor(LikeView.class));
        mapping.put(EarthKind.OFFER_KIND, getConstructor(OfferView.class));
        mapping.put(EarthKind.MESSAGE_KIND, getConstructor(MessageView.class));
        mapping.put(EarthKind.PERSON_KIND, getConstructor(PersonView.class));
        mapping.put(EarthKind.PARTY_KIND, getConstructor(PartyView.class));
        mapping.put(EarthKind.LOCATION_KIND, getConstructor(LocationView.class));
        mapping.put(EarthKind.RECENT_KIND, getConstructor(RecentView.class));
        mapping.put(EarthKind.UPDATE_KIND, getConstructor(UpdateView.class));
        mapping.put(EarthKind.JOIN_KIND, getConstructor(JoinView.class));
        mapping.put(EarthKind.RESOURCE_KIND, getConstructor(ResourceView.class));
        mapping.put(EarthKind.PROJECT_KIND, getConstructor(ProjectView.class));
    }

    public Viewable getViewForEntityOrThrow(Entity entity) {
        return getViewForEntityOrThrow(entity, EarthView.DEEP);
    }

    public Viewable getViewForEntityOrThrow(Entity entity, EarthView view) {
        Constructor<? extends Viewable> constructor = mapping.get(entity.getString(EarthField.KIND));

        if (constructor == null) {
            throw new NothingLogicResponse("earth viewer - kind does not support viewing: "
                    + entity.getString(EarthField.KIND));
        }

        try {
            return constructor.newInstance(as, entity, view);
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
