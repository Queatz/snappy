package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

import java.util.List;

/**
 * Created by jacob on 5/21/16.
 */
public class UpdateMine {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public List<Entity> updatesOf(Entity entity) {
        return earthStore.find(EarthKind.UPDATE_KIND, EarthField.TARGET, entity.key());
    }
}
