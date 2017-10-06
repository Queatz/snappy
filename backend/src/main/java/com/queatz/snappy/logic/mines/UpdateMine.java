package com.queatz.snappy.logic.mines;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;

import java.util.List;

/**
 * Created by jacob on 5/21/16.
 */
public class UpdateMine extends EarthControl {
    public UpdateMine(final EarthAs as) {
        super(as);
    }

    public List<EarthThing> updatesOf(EarthThing entity) {
        return use(EarthStore.class).find(EarthKind.UPDATE_KIND, EarthField.TARGET, entity.key());
    }
}
