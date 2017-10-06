package com.queatz.snappy.view;

import com.queatz.snappy.shared.EarthJson;

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
        return new EarthJson().toJson(this);
    }
}
