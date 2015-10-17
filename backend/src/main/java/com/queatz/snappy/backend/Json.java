package com.queatz.snappy.backend;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.annotation.Id;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.Shallow;
import com.queatz.snappy.shared.ThingSpec;

/**
 * Created by jacob on 10/14/15.
 */
public class Json {
    static ExclusionStrategy hideExclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Hide.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    };

    static ExclusionStrategy shallowExclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Id.class) == null && fieldAttributes.getAnnotation(Shallow.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    };

    static ExclusionStrategy pushExclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Id.class) == null && fieldAttributes.getAnnotation(Push.class) == null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    };

    public enum Compression {
        PUSH,
        SHALLOW,
        NONE
    }

    static public <T> T from(String json, Class<T> clazz) {
        return new GsonBuilder().create().fromJson(json, clazz);
    }

    static public String json(Object thing) {
        return json(thing, Compression.NONE);
    }

    static public String json(Object thing, Compression compression) {
        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(ThingSpec.class, ThingSpecSerializer.class)
                .addSerializationExclusionStrategy(hideExclusionStrategy);

        if (compression == Compression.SHALLOW) {
            builder.addSerializationExclusionStrategy(shallowExclusionStrategy);
        } else if (compression == Compression.PUSH) {
            builder.registerTypeAdapter(String.class, StringClipper.class);
            builder.addSerializationExclusionStrategy(pushExclusionStrategy);
        }

        return builder.create().toJson(thing);
    }
}
