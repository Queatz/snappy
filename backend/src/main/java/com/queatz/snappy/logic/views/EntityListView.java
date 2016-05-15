package com.queatz.snappy.logic.views;

import com.google.api.client.util.Lists;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Viewable;

import java.util.List;

/**
 * Created by jacob on 4/9/16.
 */
public class EntityListView implements Viewable {
    private List<Viewable> entities;

    public EntityListView(List<Entity> entities) {
        this.entities = mapToViews(entities);
    }

    private List<Viewable> mapToViews(List<Entity> entities) {
        final EarthViewer earthViewer = EarthSingleton.of(EarthViewer.class);

        List<Viewable> viewables = Lists.newArrayList();

        for (Entity entity : entities) {
            viewables.add(earthViewer.getViewForEntityOrThrow(entity));
        }

        return viewables;
    }

    @Override
    public String toJson() {
        return EarthSingleton.of(EarthJson.class).toJson(this.entities);
    }

    public List<Viewable> asList() {
        return entities;
    }
}
