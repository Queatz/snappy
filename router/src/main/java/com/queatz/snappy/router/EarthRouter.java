package com.queatz.snappy.router;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.exceptions.NothingLogicResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by jacob on 4/2/16.
 */
public class EarthRouter extends EarthControl {
    public EarthRouter(EarthAs as) {
        super(as);
    }

    private static final Map<String, Interfaceable> mapping = new HashMap<>();
    private static final Map<String, Interfaceable> specialMapping = new HashMap<>();

    public static void register(String route, Interfaceable interfaceable) {
        if (mapping.containsKey(route)) {
            throw new RuntimeException("Cannot replace route: " + route + " interface: " + interfaceable);
        }

        mapping.put(route, interfaceable);
    }

    public static void registerSpecial(String route, Interfaceable interfaceable) {
        if (specialMapping.containsKey(route)) {
            throw new RuntimeException("Cannot replace special route: " + route + " interface: " + interfaceable);
        }

        specialMapping.put(route, interfaceable);
    }

    public Interfaceable interfaceFromKindOrThrowNothingResponse(String kind) {
        Interfaceable interfaceable = mapping.get(kind);

        if (interfaceable == null) {
            throw new NothingLogicResponse("earth - no interfaceable was found");
        }

        return interfaceable;
    }

    public Interfaceable getSpecialInterfaceFromRoute0(String path0) {
        Interfaceable interfaceable = specialMapping.get(path0);

        if (interfaceable == null) {
            throw new NoSuchElementException();
        }

        return interfaceable;
    }
}
