package com.queatz.snappy.logic;

import com.queatz.snappy.logic.authorities.HubAuthority;
import com.queatz.snappy.logic.authorities.MessageAuthority;
import com.queatz.snappy.logic.authorities.OfferAuthority;
import com.queatz.snappy.logic.authorities.PartyAuthority;
import com.queatz.snappy.logic.authorities.PersonAuthority;
import com.queatz.snappy.logic.authorities.ProjectAuthority;
import com.queatz.snappy.logic.authorities.ResourceAuthority;
import com.queatz.snappy.logic.authorities.UpdateAuthority;
import com.queatz.snappy.logic.concepts.Authority;

import java.util.HashMap;
import java.util.Map;

/**
 * The class that determines whether or not you can see something.
 */
public class EarthAuthority extends EarthControl {
    public EarthAuthority(final EarthAs as) {
        super(as);
    }

    private static final Map<String, Authority> mapping = new HashMap<>();

    static {
        mapping.put(EarthKind.PERSON_KIND, new PersonAuthority());
        mapping.put(EarthKind.MESSAGE_KIND, new MessageAuthority());
        mapping.put(EarthKind.HUB_KIND, new HubAuthority());
        mapping.put(EarthKind.PROJECT_KIND, new ProjectAuthority());
        mapping.put(EarthKind.RESOURCE_KIND, new ResourceAuthority());
        mapping.put(EarthKind.UPDATE_KIND, new UpdateAuthority());
        mapping.put(EarthKind.OFFER_KIND, new OfferAuthority());
        mapping.put(EarthKind.PARTY_KIND, new PartyAuthority());
    }

    public boolean authorize(EarthThing entity, EarthRule rule) {
        // Internal access
        if (as == null || as.getUser() == null) {
            return true;
        }

        String kind = entity.getString(EarthField.KIND);
        if (mapping.containsKey(kind)) {
            return mapping.get(kind).authorize(as.getUser(), entity, rule);
        }

        // If kind has no authority rules, assume access is ok
        return true;
    }
}
