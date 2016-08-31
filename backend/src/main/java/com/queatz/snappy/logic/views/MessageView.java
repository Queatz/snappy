package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
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
    final boolean photo;

    public MessageView(EarthAs as, Entity message) {
        this(as, message, EarthView.DEEP);
    }

    public MessageView(EarthAs as, Entity message, EarthView view) {
        super(as, message, view);

        EarthStore earthStore = use(EarthStore.class);

        date = message.getDateTime(EarthField.CREATED_ON).toDate();

        if (message.contains(EarthField.MESSAGE)) {
            this.message = message.getString(EarthField.MESSAGE);
        } else {
            this.message = "";
        }

        if (message.contains(EarthField.PHOTO)) {
            photo = message.getBoolean(EarthField.PHOTO);
        } else {
            photo = false;
        }

        from = new PersonView(as, earthStore.get(message.getKey(EarthField.SOURCE)), EarthView.SHALLOW);
        to = new PersonView(as, earthStore.get(message.getKey(EarthField.TARGET)), EarthView.SHALLOW);
    }
}
