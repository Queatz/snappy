package com.queatz.snappy.logic.mines;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

import java.util.List;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceMine extends EarthControl {
    public ResourceMine(final EarthAs as) {
        super(as);
    }

    public List<EarthThing> resourcesOf(EarthThing entity) {
        return use(EarthStore.class).find(EarthKind.RESOURCE_KIND, EarthField.SOURCE, entity.key());
    }
}
