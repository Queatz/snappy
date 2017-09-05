package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.mines.ClubMine;
import com.queatz.snappy.service.ImageQueue;

import java.util.List;

/**
 * Created by jacob on 4/3/16.
 */
public class ThingView extends ExistenceView {

    final String name;
    final String about;
    final boolean photo;
    final String placeholder;
    final Float aspect;
    final Boolean owner;
    final Boolean hidden;
    final List<Viewable> clubs;

    public ThingView(EarthAs as, EarthThing thing) {
        this(as, thing, EarthView.DEEP);
    }

    public ThingView(EarthAs as, EarthThing thing, EarthView view) {
        super(as, thing, view);

        if (thing.has(EarthField.NAME)) {
            name = thing.getString(EarthField.NAME);
        } else {
            name = null;
        }

        photo = thing.has(EarthField.PHOTO) && thing.getBoolean(EarthField.PHOTO);

        if (thing.has(EarthField.ABOUT)) {
            about = thing.getString(EarthField.ABOUT);
        } else {
            about = null;
        }

        String spacer = null;

        if (photo) {
            if (thing.has(EarthField.PLACEHOLDER)) {
                spacer = thing.getString(EarthField.PLACEHOLDER);
            } else {
                ImageQueue.getService().enqueue(thing.key().name());
            }
        }

        placeholder = spacer;

        if (placeholder != null && thing.has(EarthField.ASPECT_RATIO)) {
            aspect = (float) thing.getDouble(EarthField.ASPECT_RATIO);
        } else {
            aspect = null;
        }

        owner = as.hasUser() &&
                (thing.key().name().equals(as.getUser().key().name()) ||
                        (thing.has(EarthField.SOURCE) && thing.getString(EarthField.SOURCE).equals(as.getUser().key().name())));

        if (owner) {
            hidden = thing.has(EarthField.HIDDEN) && thing.getBoolean(EarthField.HIDDEN);
        } else {
            hidden = null;
        }

        if (EarthKind.CLUB_KIND.equals(thing.getString(EarthField.KIND))) {
            clubs = null;
        } else {
            clubs = new EntityListView(
                    as,
                    use(ClubMine.class).clubsOf(thing),
                    EarthView.SHALLOW
            ).asList();
        }
    }
}
