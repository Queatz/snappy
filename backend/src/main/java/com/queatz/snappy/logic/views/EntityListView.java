package com.queatz.snappy.logic.views;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.view.EarthView;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.view.Viewable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 4/9/16.
 */
public class EntityListView extends EarthControl implements Viewable {
    private List<Viewable> entities;

    public EntityListView(EarthAs as, List<EarthThing> entities) {
        this(as, entities, EarthView.DEEP);
    }

    public EntityListView(EarthAs as, List<EarthThing> entities, EarthView view) {
        super(as);
        this.entities = mapToViews(entities, view);
    }

    private List<Viewable> mapToViews(List<EarthThing> entities, EarthView view) {
        final EarthViewer earthViewer = use(EarthViewer.class);

        List<Viewable> viewables = new ArrayList<>();

        for (EarthThing entity : entities) {
            viewables.add(earthViewer.getViewForEntityOrThrow(entity, view));
        }

        return viewables;
    }

    @Override
    public String toJson() {
        return new EarthJson().toJson(this.entities);
    }

    public List<Viewable> asList() {
        return entities;
    }
}
