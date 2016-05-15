package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/14/16.
 */
public class PersonMine {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity byEmail(String email) {
        QueryResults<Entity> results = earthStore.query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.PERSON_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.EMAIL, email)
        );

        if (results.hasNext()) {
            return results.next();
        } else {
            return null;
        }
    }

    public Entity byToken(String token) {
        QueryResults<Entity> results = earthStore.query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.PERSON_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.TOKEN, token)
        );

        if (results.hasNext()) {
            return results.next();
        } else {
            return null;
        }
    }

    public Entity byGoogleUrl(String googleUrl) {
        QueryResults<Entity> results = earthStore.query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.PERSON_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.GOOGLE_URL, googleUrl)
        );

        if (results.hasNext()) {
            return results.next();
        } else {
            return null;
        }
    }
}
