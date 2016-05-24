package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

import java.util.List;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceMine {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public List<Entity> resourcesOf(Entity entity) {
        return earthStore.find(EarthKind.RESOURCE_KIND, EarthField.SOURCE, entity.key());
    }
}
