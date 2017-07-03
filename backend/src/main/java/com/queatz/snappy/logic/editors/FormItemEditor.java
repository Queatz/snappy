package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

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