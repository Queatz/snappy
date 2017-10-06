package com.queatz.snappy.logic.editors;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 6/4/17.
 */

public class FormItemEditor extends EarthControl {
    public FormItemEditor(@NotNull EarthAs as) {
        super(as);
    }

    public EarthThing newFormItem(@NotNull EarthThing form, @NotNull String type) {
        EarthStore earthStore = use(EarthStore.class);

        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.FORM_ITEM_KIND))
                .set(EarthField.TYPE, type)
                .set(EarthField.TARGET, form.key().name()));
    }
}
