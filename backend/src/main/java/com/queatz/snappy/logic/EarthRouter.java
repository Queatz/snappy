package com.queatz.snappy.logic;

import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.special.HereInterface;
import com.queatz.snappy.logic.things.HubInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by jacob on 4/2/16.
 */
public class EarthRouter {
    private static final Map<String, Interfaceable> mapping = new HashMap<>();
    private static final Map<String, Interfaceable> specialMapping = new HashMap<>();

    static {

        /**
         * This is the kinds to interfaces mapping!
         */
        mapping.put(EarthKind.HUB_KIND, new HubInterface());

        /**
         * This is the mapping for special routes, such as /here and /me.
         */
        specialMapping.put(EarthSpecialRoute.HERE_ROUTE, new HereInterface());
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
