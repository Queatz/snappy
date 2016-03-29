package com.queatz.snappy.earth.concept;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.snappy.earth.access.As;
import com.queatz.snappy.earth.access.NothingEarthException;
import com.queatz.snappy.earth.access.UnsupportedConceptEarthException;
import com.queatz.snappy.earth.thing.Existence;
import com.queatz.snappy.earth.util.AnnotationMap;
import com.queatz.snappy.earth.util.AnnotationValueMapper;
import com.queatz.snappy.earth.util.ExistenceViewAnnotationMapper;
import com.queatz.snappy.earth.util.MethodAnnotationMap;
import com.queatz.snappy.earth.view.ExistenceView;
import com.queatz.snappy.earth.view.KindViewGetter;
import com.queatz.snappy.earth.view.KindViewSetter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 3/26/16.
 */
public class ViewConcept extends Concept {

    final static private Map<Class<? extends Existence>, Class<? extends ExistenceView>> existenceViews =
            AnnotationMap.create(ExistenceView.class, new ExistenceViewAnnotationMapper());

    final static private Map<Class<? extends Existence>, Map<String, Method>> existenceViewGetters =
            MethodAnnotationMap.create(ExistenceView.class,
                    KindViewGetter.class,
                    new AnnotationValueMapper<KindView, Class<? extends Existence>>() {
                        @Override
                        public Class<? extends Existence> map(KindView annotation) {
                            return annotation.value();
                        }
                    },
                    new AnnotationValueMapper<KindViewGetter, String>() {
                        @Override
                        public String map(KindViewGetter annotation) {
                            return annotation.value();
                        }
                    });

    final static private Map<Class<? extends Existence>, Map<String, Method>> existenceViewSetters =
            MethodAnnotationMap.create(ExistenceView.class,
                    KindViewSetter.class,
                    new AnnotationValueMapper<KindView, Class<? extends Existence>>() {
                        @Override
                        public Class<? extends Existence> map(KindView annotation) {
                            return annotation.value();
                        }
                    },
                    new AnnotationValueMapper<KindViewSetter, String>() {
                        @Override
                        public String map(KindViewSetter annotation) {
                            return annotation.value();
                        }
                    });


    final Gson gson = newGsonBuilder().create();

    private GsonBuilder newGsonBuilder() {
        return new GsonBuilder().setDateFormat(DateFormat.LONG, DateFormat.LONG);
    }

    public class ThingReader<T extends Existence> {

        private T thing;

        private ThingReader(T thing) {
            this.thing = thing;
        }

        public String json() {
            return gson.toJson(getJsonTree());
        }

        @SuppressWarnings("unchecked")
        @Nonnull
        private JsonElement getJsonTree() {
            Class<? extends ExistenceView> viewClass = existenceViews.get(thing.getClass());

            if (viewClass == null) {
                throw new UnsupportedConceptEarthException();
            }

            JsonObject json = new JsonObject();

            Map<String, Method> getters = existenceViewGetters.get(thing.getClass());

            for (Map.Entry<String, Method> getter : getters.entrySet()) {
                Object value;

                try {
                    value = getter.getValue().invoke(viewClass);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

                // TODO use our own mapper that looks for Existence types returned,
                // TODO and then does a ThingReader on them...not always json!

                json.add(getter.getKey(), gson.toJsonTree(value));
            }

            return json;
        }
    }

    public class ThingWriter<T extends Existence> {
        private T thing;

        private ThingWriter(T thing) {
            this.thing = thing;
        }

        @SuppressWarnings("unchecked")
        public void json(Object object) {
            JsonElement json = gson.toJsonTree(object);

            if (!json.isJsonObject()) {
                throw new NothingEarthException();
            }

            Class<? extends ExistenceView> viewClass = existenceViews.get(thing.getClass());

            if (viewClass == null) {
                throw new UnsupportedConceptEarthException();
            }

            ExistenceView view;

            try {
                view = viewClass.getConstructor(thing.getClass()).newInstance(thing);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            Map<String, Method> setters = existenceViewSetters.get(thing.getClass());

            // TODO read JSON tree
            for (Map.Entry<String, JsonElement> role : ((JsonObject) json).entrySet()) {
                Method setter = setters.get(role.getKey());

                if (setter == null) {
                    Logger.getGlobal().warning("No setter found. sought=" + role.getKey());
                    continue;
                }

                try {
                    setter.invoke(view, gson.fromJson(role.getValue(), setter.getGenericParameterTypes()[0]));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public ViewConcept(As as) {
        super(as);
    }

    public <T extends Existence> ThingReader<T> read(T thing) {
        return new ThingReader<>(thing);
    }

    public <T extends Existence> ThingWriter<T> write(T thing) {
        return new ThingWriter<>(thing);
    }
}
