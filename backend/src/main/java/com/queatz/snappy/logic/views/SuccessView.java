package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.concepts.Viewable;

/**
 * Created by jacob on 5/14/16.
 */
public class SuccessView implements Viewable {
    final boolean success;

    public SuccessView(boolean success) {
        this.success = success;
    }

    @Override
    public String toJson() {
        return EarthSingleton.of(EarthJson.class).toJson(this);
    }
}
