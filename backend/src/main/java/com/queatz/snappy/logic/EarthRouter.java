package com.queatz.snappy.logic;

import com.queatz.snappy.logic.interfaces.ByNameInterface;
import com.queatz.snappy.logic.interfaces.ContactInterface;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.interfaces.HereInterface;
import com.queatz.snappy.logic.interfaces.EndorsementInterface;
import com.queatz.snappy.logic.interfaces.FollowerInterface;
import com.queatz.snappy.logic.interfaces.HubInterface;
import com.queatz.snappy.logic.interfaces.JoinInterface;
import com.queatz.snappy.logic.interfaces.LikeInterface;
import com.queatz.snappy.logic.interfaces.LocationInterface;
import com.queatz.snappy.logic.interfaces.MeInterface;
import com.queatz.snappy.logic.interfaces.MessageInterface;
import com.queatz.snappy.logic.interfaces.OfferInterface;
import com.queatz.snappy.logic.interfaces.PersonInterface;
import com.queatz.snappy.logic.interfaces.ProjectInterface;
import com.queatz.snappy.logic.interfaces.RecentInterface;
import com.queatz.snappy.logic.interfaces.ResourceInterface;
import com.queatz.snappy.logic.interfaces.UpdateInterface;

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
        mapping.put(EarthKind.CONTACT_KIND, new ContactInterface());
        mapping.put(EarthKind.FOLLOWER_KIND, new FollowerInterface());
        mapping.put(EarthKind.LIKE_KIND, new LikeInterface());
        mapping.put(EarthKind.OFFER_KIND, new OfferInterface());
        mapping.put(EarthKind.LOCATION_KIND, new LocationInterface());
        mapping.put(EarthKind.MESSAGE_KIND, new MessageInterface());
        mapping.put(EarthKind.PERSON_KIND, new PersonInterface());
        mapping.put(EarthKind.RECENT_KIND, new RecentInterface());
        mapping.put(EarthKind.UPDATE_KIND, new UpdateInterface());
        mapping.put(EarthKind.JOIN_KIND, new JoinInterface());
        mapping.put(EarthKind.ENDORSEMENT_KIND, new EndorsementInterface());
        mapping.put(EarthKind.RESOURCE_KIND, new ResourceInterface());
        mapping.put(EarthKind.PROJECT_KIND, new ProjectInterface());

        /**
         * This is the mapping for special routes, such as /here and /me.
         */
        specialMapping.put(EarthSpecialRoute.HERE_ROUTE, new HereInterface());
        specialMapping.put(EarthSpecialRoute.ME_ROUTE, new MeInterface());
        specialMapping.put(EarthSpecialRoute.BY_NAME_ROUTE, new ByNameInterface());
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
