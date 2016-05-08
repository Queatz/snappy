package com.queatz.snappy.logic.view;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Viewable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 4/9/16.
 */
public class EnitityListView implements Viewable {

    private EarthViewer earthViewer = EarthSingleton.of(EarthViewer.class);
    private List<Viewable> entities;

    public EnitityListView(List<Entity> entities) {
        this.entities = mapToViews(entities);
    }

    private List<Viewable> mapToViews(List<Entity> entities) {
        List<Viewable> viewables = new ArrayList<>();

        for (Entity entity : entities) {
            viewables.add(earthViewer.getViewForEntityOrThrow(entity));
        }

        return viewables;
    }

    @Override
    public String toJson() {
        return EarthSingleton.of(EarthJson.class).toJson(this.entities);
    }
}
