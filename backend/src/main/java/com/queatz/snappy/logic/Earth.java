package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 4/1/16.
 *
 * This is the main entry point for all end-user actionables.
 *
 * Example 1:
 *
 * GET /1
 *  -> {id: '1', kind: 'hub'}
 *
 * Example 2:
 *
 * POST /1?name=Rosie's Flower Garden
 *  -> HubInterface
 *  -> Hub(1)
 *  -> Set name to 'Rosie\'s Flower Garden'
 *
 * Example 3:
 *
 * POST /?kind=hub -> { hub-definition }
 */
public class Earth extends EarthControl implements Interfaceable {
    private final EarthRouter earthRouter;

    public Earth(EarthAs as) {
        super(as);

        earthRouter = use(EarthRouter.class);
    }

    @Override
    public String get(EarthAs as) {
        // Special routes override all
        if (!as.getRoute().isEmpty()) try {
            return earthRouter.getSpecialInterfaceFromRoute0(as.getRoute().get(0)).get(as);
        } catch (NoSuchElementException ignored) {}

        // Otherwise assume it's an ID
        final Entity entity = getEntityFromRoute(as.getRoute());
        final Interfaceable interfaceable = getInterfacableFromEntity(entity);

        return interfaceable.get(as);
    }

    @Override
    public String post(EarthAs as) {
        // Special routes override all
        if (!as.getRoute().isEmpty()) try {
            return earthRouter.getSpecialInterfaceFromRoute0(as.getRoute().get(0)).post(as);
        } catch (NoSuchElementException ignored) {}

        final Interfaceable interfaceable;

        // Otherwise assume it's an ID, or a create request with a kind supplied in the parameters
        if (as.getRoute().isEmpty()) {
            interfaceable = getInterfacableFromParameters(as.getParameters());
        } else {
            Entity entity = getEntityFromRoute(as.getRoute());
            interfaceable = getInterfacableFromEntity(entity);
        }

        return interfaceable.post(as);
    }

    @Nonnull
    private Entity getEntityFromRoute(@Nonnull List<String> route) {
        if (route.isEmpty()) {
            throw new NothingLogicResponse("earth - route is empty");
        }

        final String id = route.get(0);

        if (id.isEmpty()) {
            throw new NothingLogicResponse("earth - id is empty");
        }

        Entity entity = use(EarthStore.class).get(id);

        if (entity == null) {
            throw new NothingLogicResponse("earth - no entity was found");
        }

        return entity;
    }

    private Interfaceable getInterfacableFromParameters(@Nonnull Map<String, String[]> parameters) {
        final String[] kindParameter = parameters.get(EarthField.KIND);

        if (kindParameter == null) {
            throw new NothingLogicResponse("earth - no kind parameter was supplied");
        }

        if (kindParameter.length != 1) {
            throw new NothingLogicResponse("earth - no kind parameter value was found");
        }

        final String kind = kindParameter[0];

        if (kind == null || kind.isEmpty()) {
            throw new NothingLogicResponse("earth - kind parameter has no values");
        }

        return earthRouter.interfaceFromKindOrThrowNothingResponse(kind);
    }

    @Nonnull
    private Interfaceable getInterfacableFromEntity(@Nonnull Entity entity) {
        final String kind = entity.getString(EarthField.KIND);

        if (kind == null || kind.isEmpty()) {
            throw new NothingLogicResponse("earth - entity does not have a kind");
        }

        return earthRouter.interfaceFromKindOrThrowNothingResponse(kind);
    }
}
