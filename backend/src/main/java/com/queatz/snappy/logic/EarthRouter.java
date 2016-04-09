package com.queatz.snappy.logic;

import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.things.HubInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 4/2/16.
 */
public class EarthRouter {
    private static Map<String, Interfaceable> mapping = new HashMap<>();

    static {

        /**
         * This is the kinds to interfaces mapping!
         */
        mapping.put(EarthKind.HUB_KIND, new HubInterface());
    }

    public Interfaceable interfaceFromKindOrThrowNothingResponse(String kind) {
        Interfaceable interfaceable = mapping.get(kind);

        if (interfaceable == null) {
            throw new NothingLogicResponse("earth - no interfaceable was found");
        }

        return interfaceable;
    }
}
