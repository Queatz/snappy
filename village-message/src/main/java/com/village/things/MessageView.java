package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.view.EarthView;
import com.queatz.snappy.view.EarthViewer;
import com.queatz.snappy.view.Viewable;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class MessageView extends ExistenceView {

    final Date date;
    final String message;
    final Viewable from;
    final Viewable to;
    final boolean photo;

    public MessageView(EarthAs as, EarthThing message) {
        this(as, message, EarthView.DEEP);
    }

    public MessageView(EarthAs as, EarthThing message, EarthView view) {
        super(as, message, view);

        EarthStore earthStore = use(EarthStore.class);

        date = message.getDate(EarthField.CREATED_ON);

        if (message.has(EarthField.MESSAGE)) {
            this.message = message.getString(EarthField.MESSAGE);
        } else {
            this.message = "";
        }

        if (message.has(EarthField.PHOTO)) {
            photo = message.getBoolean(EarthField.PHOTO);
        } else {
            photo = false;
        }

        from = use(EarthViewer.class).getViewForEntityOrThrow(earthStore.get(message.getKey(EarthField.SOURCE)), EarthView.SHALLOW);
        to = use(EarthViewer.class).getViewForEntityOrThrow(earthStore.get(message.getKey(EarthField.TARGET)), EarthView.SHALLOW);
    }
}
