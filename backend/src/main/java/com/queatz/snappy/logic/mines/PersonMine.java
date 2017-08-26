package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

import java.util.List;

/**
 * Created by jacob on 5/14/16.
 */
public class PersonMine extends EarthControl {
    public PersonMine(final EarthAs as) {
        super(as);
    }

    /**
     * Authentication not required. Be careful!
     */
    public EarthThing byEmail(String email) {
        List<EarthThing> result = use(EarthStore.class).queryInternal(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.EMAIL + " == @email",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.PERSON_KIND,
                        "email", email
                )
                , 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * Authentication not required. Be careful!
     */
    public EarthThing byToken(String token) {
        List<EarthThing> result = use(EarthStore.class).queryInternal(
                "x." + EarthField.KIND + " == @kind and " +
                "x." + EarthField.TOKEN + " == @token",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.PERSON_KIND,
                        "token", token
                )
                , 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public EarthThing byGoogleUrl(String googleUrl) {
        List<EarthThing> result = use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                "x." + EarthField.GOOGLE_URL + " == @google_url ",
                ImmutableMap.of(
                        "kind", EarthKind.PERSON_KIND,
                        "google_url", googleUrl
                )
                , 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public long countBySubscription(String subscription) {
        return  use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.SUBSCRIPTION + " == @subscription",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.PERSON_KIND,
                        "subscription", subscription
                )
                , 1).size();
    }
}
