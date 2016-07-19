package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/14/16.
 */
public class EndorsementEditor extends EarthControl {
    private final EarthStore earthStore;

    public EndorsementEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public Entity newEndorsement(Entity person, Entity thing) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.LIKE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.TARGET, thing.key()));
    }
}
