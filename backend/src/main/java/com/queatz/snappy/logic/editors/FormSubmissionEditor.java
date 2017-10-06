package com.queatz.snappy.logic.editors;

import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.api.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 6/4/17.
 */

public class FormSubmissionEditor extends EarthControl {
    public FormSubmissionEditor(@NotNull EarthAs as) {
        super(as);
    }

    public EarthThing newFormSubmission(@NotNull EarthThing form, @NotNull String data) {
        EarthStore earthStore = use(EarthStore.class);

        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.FORM_SUBMISSION_KIND))
                .set(EarthField.SOURCE)
                .set(EarthField.HIDDEN, true)
                .set(EarthField.TARGET, form.key().name())
                .set(EarthField.DATA, data));
    }
}
