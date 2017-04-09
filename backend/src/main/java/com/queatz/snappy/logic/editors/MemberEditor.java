package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.shared.Config;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 4/9/17.
 */

public class MemberEditor extends EarthControl {
    public MemberEditor(@NotNull EarthAs as) {
        super(as);
    }

    public EarthThing create(EarthThing source, EarthThing target, String role) {
        EarthStore earthStore = use(EarthStore.class);

        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.MEMBER_KIND))
                .set(EarthField.STATUS, Config.MEMBER_STATUS_ACTIVE)
                .set(EarthField.ROLE, role)
                .set(EarthField.SOURCE, source.key())
                .set(EarthField.TARGET, target.key()));
    }
}
