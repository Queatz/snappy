package com.queatz.snappy.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jacob on 4/2/16.
 */
public class EarthSingleton {
    private static Map<Class, Object> singletons;

    static {
        singletons = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static <T> T of(Class<T> clazz) {
        if (!singletons.containsKey(clazz)) {
            try {
                singletons.put(clazz, clazz.newInstance());
            } catch (InstantiationException e) {
                Logger.getGlobal().log(Level.SEVERE, "Singleton said nope for " + clazz.getSimpleName(), e);
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                Logger.getGlobal().log(Level.SEVERE, "Singleton said nope for " + clazz.getSimpleName(), e);
                throw new RuntimeException(e);
            }
        }

        return (T) singletons.get(clazz);
    }
}
