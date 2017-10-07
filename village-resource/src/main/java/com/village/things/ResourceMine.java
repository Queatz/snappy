package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;

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
