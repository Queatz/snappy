package com.queatz.snappy.logic.view;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.concepts.Viewable;

/**
 * Created by jacob on 4/3/16.
 */
public class ExistenceView implements Viewable {

    final String id;
    final String kind;

    public ExistenceView(Entity entity) {
        id = entity.key().name();
        kind = entity.getString(EarthField.KIND);
    }

    @Override
    public String toJson() {
        return EarthSingleton.of(EarthJson.class).toJson(this);
    }
}
