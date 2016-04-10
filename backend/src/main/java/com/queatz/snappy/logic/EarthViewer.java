package com.queatz.snappy.logic;

import com.google.gcloud.datastore.Entity;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.view.HubView;

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
