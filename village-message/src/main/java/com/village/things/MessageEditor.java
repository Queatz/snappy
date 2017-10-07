package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

/**
 * Created by jacob on 5/8/16.
 */
public class MessageEditor extends EarthControl {
    private final EarthStore earthStore;

    public MessageEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newMessage(EarthThing source, EarthThing target, String message, boolean photo) {
        EarthThing.Builder builder = earthStore.edit(earthStore.create(EarthKind.MESSAGE_KIND))
                .set(EarthField.SOURCE, source.key())
                .set(EarthField.TARGET, target.key())
                .set(EarthField.MESSAGE, message)
                .set(EarthField.PHOTO, photo);

        return earthStore.save(builder);
    }

    public EarthThing stageMessage(EarthThing source, EarthThing target) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.MESSAGE_KIND))
                .set(EarthField.SOURCE, source.key())
                .set(EarthField.TARGET, target.key()));
    }

    public EarthThing setMessage(EarthThing message, String text, boolean photo) {
        return earthStore.save(earthStore.edit(message)
                .set(EarthField.MESSAGE, text)
                .set(EarthField.PHOTO, photo));
    }
}
