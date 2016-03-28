package com.queatz.snappy.earth.view;

import com.queatz.snappy.earth.access.NothingEarthException;
import com.queatz.snappy.earth.thing.Existence;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 3/27/16.
 */
public class ExistenceView {

    private Existence existence;

    public ExistenceView(@Nonnull Existence existence) {
        this.existence = existence;
    }

    @KindViewGetter("id")
    public String getId() {
        return existence.getId();
    }

    @KindViewSetter("id")
    public void setId(String id) {
        if (existence.getId() != null) {
            throw new NothingEarthException();
        }

        existence.setId(id);
    }
}
