package com.queatz.snappy.earth.view;

import com.queatz.snappy.earth.concept.KindView;
import com.queatz.snappy.earth.thing.Thing;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 3/27/16.
 */

public class ThingView extends ExistenceView {

    private Thing thing;

    public ThingView(@Nonnull Thing thing) {
        super(thing);
        this.thing = thing;
    }

    @KindViewGetter("description")
    public String getDescription() {
        return thing.getDescription();
    }

    @KindViewSetter("description")
    public void setDescription(String description) {
        thing.setDescription(description);
    }

    @KindViewGetter("name")
    public String getName() {
        return thing.getName();
    }

    @KindViewSetter("name")
    public void setName(String name) {
        thing.setName(name);
    }

    @KindViewGetter("photo")
    public boolean hasPhoto() {
        return thing.isPhoto();
    }

    @KindViewSetter("photo")
    public void setPhoto(boolean photo) {
        thing.setPhoto(photo);
    }
}
