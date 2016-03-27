package com.queatz.snappy.earth.access;

import com.queatz.snappy.earth.concept.Concept;
import com.queatz.snappy.shared.things.PersonSpec;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by jacob on 3/26/16.
 */
public class As {
    private final PersonSpec person;

    public As(PersonSpec person) {
        this.person = person;
    }

    public <T extends Concept> T concept(Class<T> clazz) {
        try {
            return clazz.getConstructor(As.class).newInstance(this);
        } catch (NoSuchMethodException |
                IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public PersonSpec getPerson() {
        return this.person;
    }
}
