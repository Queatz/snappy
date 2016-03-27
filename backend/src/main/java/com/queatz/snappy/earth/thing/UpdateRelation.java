package com.queatz.snappy.earth.thing;

import com.googlecode.objectify.annotation.Entity;
import com.queatz.snappy.earth.concept.KindView;
import com.queatz.snappy.earth.view.ExistenceView;

/**
 * Created by jacob on 3/26/16.
 */

@Entity
@Kind("update")
@KindView(ExistenceView.class)
public class UpdateRelation extends Relation {
    private String message;
    private boolean photo;

    public String getMessage() {
        return message;
    }

    public UpdateRelation setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isPhoto() {
        return photo;
    }

    public UpdateRelation setPhoto(boolean photo) {
        this.photo = photo;
        return this;
    }
}
