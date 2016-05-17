package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class MessageView extends ExistenceView {

    final Date date;
    final String message;
    final PersonView from;
    final PersonView to;

    public MessageView(Entity message) {
        this(message, EarthView.DEEP);
    }

    public MessageView(Entity message, EarthView view) {
        super(message, view);

        EarthStore earthStore = EarthSingleton.of(EarthStore.class);

        date = message.getDateTime(EarthField.CREATED_ON).toDate();
        this.message = message.getString(EarthField.MESSAGE);
        from = new PersonView(earthStore.get(message.getKey(EarthField.SOURCE)), EarthView.SHALLOW);
        to = new PersonView(earthStore.get(message.getKey(EarthField.TARGET)), EarthView.SHALLOW);
    }
}
