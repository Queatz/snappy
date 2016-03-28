package com.queatz.snappy.earth.util;

import com.queatz.snappy.earth.concept.KindView;
import com.queatz.snappy.earth.thing.Existence;
import com.queatz.snappy.earth.view.ExistenceView;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * Created by jacob on 3/27/16.
 */
public class ExistenceViewAnnotationMapper implements AnnotationMapper<KindView, Class<? extends ExistenceView>, Class<? extends Existence>, Class<? extends ExistenceView>> {

    @Override
    public Map.Entry<Class<? extends Existence>, Class<? extends ExistenceView>> map(KindView annotation, Class<? extends ExistenceView> clazz) {
        return Pair.<Class<? extends Existence>, Class<? extends ExistenceView>>of(annotation.value(), clazz);
    }
}