package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;

/**
 * Created by jacob on 5/14/16.
 */
public class JoinView extends LinkView {

    final String status;

    public JoinView(Entity join) {
        super(join);
        status = join.getString(EarthField.STATUS);
    }
}
