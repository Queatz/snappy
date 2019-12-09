package com.queatz.snappy.view;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.exceptions.NothingLogicResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 4/9/16.
 *
 * @deprecated see {@code GraphView}
 */
public class EarthViewer extends EarthControl {
    public EarthViewer(final EarthAs as) {
        super(as);
    }

    private static final Map<String, Constructor<? extends Viewable>> mapping = new HashMap<>();

    private static <T> Constructor<T> getConstructor(Class<T> clazz) {
        try {
            return clazz.getConstructor(EarthAs.class, EarthThing.class, EarthView.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static void register(String kind, Class<? extends Viewable> viewable) {
        if (mapping.containsKey(kind)) {
            throw new RuntimeException("Cannot replace view for kind: " + kind + " class: " + viewable);
        }

        mapping.put(kind, getConstructor(viewable));
    }

    public Viewable getViewForEntityOrThrow(EarthThing entity) {
        return getViewForEntityOrThrow(entity, EarthView.DEEP);
    }

    public Viewable getViewForEntityOrThrow(EarthThing entity, EarthView view) {
        if (entity == null) {
            return null;
        }

        Constructor<? extends Viewable> constructor = mapping.get(entity.getString(EarthField.KIND));

        if (constructor == null) {
            throw new NothingLogicResponse("earth viewer - kind does not support viewing: "
                    + entity.getString(EarthField.KIND));
        }

        Exception error = null;

        try {
            return constructor.newInstance(as, entity, view);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            error = e;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            error = e;
        } catch (InstantiationException e) {
            e.printStackTrace();
            error = e;
        }

        throw new NothingLogicResponse("earth viewer - failed to create view for kind: "
                + entity.getString(EarthField.KIND) + "\n\n" + EarthViewer.getStackTrace(error));
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
