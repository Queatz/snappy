package com.queatz.snappy.earth.concept;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.queatz.snappy.earth.access.As;
import com.queatz.snappy.earth.access.UnsupportedConceptEarthException;
import com.queatz.snappy.earth.thing.Existence;
import com.queatz.snappy.earth.util.ExistenceAnnotationMap;
import com.queatz.snappy.earth.util.ExistenceViewAnnotationMapper;
import com.queatz.snappy.earth.view.ExistenceView;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 3/26/16.
 */
public class ViewConcept extends Concept {

    final static private Map<Class<? extends Existence>, Class<? extends ExistenceView>> existenceViews =
            ExistenceAnnotationMap.create(new ExistenceViewAnnotationMapper());

    final Gson gson = newGsonBuilder().create();

    private GsonBuilder newGsonBuilder() {
        return new GsonBuilder().setDateFormat(DateFormat.LONG, DateFormat.LONG);
    }

    public class ThingReader<T extends Existence, V extends ExistenceView> {
        private T thing;

        private ThingReader(T thing) {
            this.thing = thing;
        }

        public String json() {
            return gson.toJson(getView());
        }

        public Object object() {
            return getView();
        }

        @SuppressWarnings("unchecked")
        @Nonnull
        private V getView() {
            Class<V> viewClass = (Class<V>) existenceViews.get(thing.getClass());

            if (viewClass == null) {
                throw new UnsupportedConceptEarthException();
            }

            return gson.fromJson(gson.toJsonTree(thing), viewClass);
        }
    }

    public class ThingWriter<T extends Existence, V extends ExistenceView> {
        private T thing;

        private ThingWriter(T thing) {
            this.thing = thing;
        }

        @SuppressWarnings("unchecked")
        public void json(Object json) {
            Class<V> viewClass = (Class<V>) existenceViews.get(thing.getClass());

            if (viewClass == null) {
                throw new UnsupportedConceptEarthException();
            }

            final JsonObject jsonObject = gson.toJsonTree(json).getAsJsonObject();

            final InstanceCreator<T> theThing = new InstanceCreator<T>() {
                @Override
                public T createInstance(Type type) {
                    return thing;
                }
            };

            Gson gsonUpdate = newGsonBuilder()
                    .registerTypeAdapter(thing.getClass(), theThing)
                    .create();

            gsonUpdate.fromJson(jsonObject, thing.getClass());
        }
    }

    public ViewConcept(As as) {
        super(as);
    }

    public <T extends Existence, V extends ExistenceView> ThingReader<T, V> read(T thing) {
        return new ThingReader<>(thing);
    }

    public <T extends Existence, V extends ExistenceView> ThingWriter<T, V> write(T thing) {
        return new ThingWriter<>(thing);
    }
}
