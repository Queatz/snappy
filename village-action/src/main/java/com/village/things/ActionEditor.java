package com.village.things;

import com.google.common.base.Strings;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

/**
 * Created by jacob on 10/11/17.
 */

public class ActionEditor extends EarthControl {
    private final EarthStore earthStore;

    public ActionEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newAction(EarthThing source, EarthThing target, String role, String type, String data, String token) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.ACTION_KIND))
                .set(EarthField.SOURCE, source.key())
                .set(EarthField.TARGET, target.key())
                .set(EarthField.TYPE, type)
                .set(EarthField.DATA, data)
                .set(EarthField.MESSAGE, "")
                .set(EarthField.TOKEN, token == null ? "" :token)
                .set(EarthField.ROLE, role));
    }

    public EarthThing edit(EarthThing action, String role, String type, String data, String token) {
        EarthThing.Builder builder = earthStore.edit(action);

        if (!Strings.isNullOrEmpty(role)) {
            builder.set(EarthField.ROLE, role);
        }

        if (!Strings.isNullOrEmpty(type)) {
            builder.set(EarthField.TYPE, type);
        }

        if (!Strings.isNullOrEmpty(data)) {
            builder.set(EarthField.DATA, data);
        }

        if (!Strings.isNullOrEmpty(token)) {
            builder.set(EarthField.TOKEN, token);
        }

        return earthStore.save(builder);
    }

    public EarthThing setValue(EarthThing action, String value) {
        return earthStore.save(earthStore.edit(action).set(EarthField.MESSAGE, value));
    }
}
