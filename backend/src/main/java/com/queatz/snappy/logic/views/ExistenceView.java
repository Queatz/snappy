package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;

/**
 * Created by jacob on 4/3/16.
 */
public class ExistenceView extends EarthControl implements Viewable {

    /**
     * Allows association with the client's local ID.  Usually used when creating new objects.
     */
    String localId = null;

    final String id;
    final String kind;

    public ExistenceView(EarthAs as, EarthThing entity) {
        this(as, entity, EarthView.DEEP);
    }

    public ExistenceView(EarthAs as, EarthThing entity, EarthView view) {
        super(as);

        id = entity.key().name();
        kind = entity.getString(EarthField.KIND);
        localId = entity.getLocalId();
    }

    @Override
    public String toJson() {
        return new EarthJson().toJson(this);
    }

    public ExistenceView setLocalId(String localId) {
        this.localId = localId;
        return this;
    }
}
