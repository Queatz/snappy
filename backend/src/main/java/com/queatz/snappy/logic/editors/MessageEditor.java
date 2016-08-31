package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.BaseEntity;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.NullValue;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/8/16.
 */
public class MessageEditor extends EarthControl {
    private final EarthStore earthStore;

    public MessageEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public Entity newMessage(Entity source, Entity target, String message, boolean photo) {
        Entity.Builder builder = earthStore.edit(earthStore.create(EarthKind.MESSAGE_KIND))
                .set(EarthField.SOURCE, source.key())
                .set(EarthField.TARGET, target.key())
                .set(EarthField.MESSAGE, message)
                .set(EarthField.PHOTO, photo);

        return earthStore.save(builder);
    }

    public Entity stageMessage(Entity source, Entity target) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.MESSAGE_KIND))
                .set(EarthField.SOURCE, source.key())
                .set(EarthField.TARGET, target.key()));
    }

    public Entity setMessage(Entity message, String text, boolean photo) {
        return earthStore.save(earthStore.edit(message)
                .set(EarthField.MESSAGE, text)
                .set(EarthField.PHOTO, photo));
    }
}
