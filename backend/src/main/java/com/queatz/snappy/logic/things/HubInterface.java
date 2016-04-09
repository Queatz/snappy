package com.queatz.snappy.logic.things;

import com.google.gcloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.HubEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.view.HubView;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 4/1/16.
 */

public class HubInterface implements Interfaceable {

    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    private final HubEditor hubEditor = EarthSingleton.of(HubEditor.class);

    @Override
    public String get(@Nonnull List<String> route, @Nonnull Map<String, String[]> parameters) {
        if (route.isEmpty()) {
            throw new NothingLogicResponse("hub - empty route");
        }

        Entity hub = earthStore.get(route.get(0));

        return new HubView(hub).toJson();
    }

    @Override
    public String post(@Nonnull List<String> route, @Nonnull Map<String, String[]> parameters) {
        if (route.isEmpty()) {
            String[] name = parameters.get(EarthField.NAME);
            String[] about = parameters.get(EarthField.ABOUT);

            if (name == null || about == null || name.length != 1 || about.length != 1) {
                throw new NothingLogicResponse("hub - no name or about parameters");
            }

            Entity hub = hubEditor.newHub(name[0], about[0]);

            return new HubView(hub).toJson();
        }

        return null;
    }
}
