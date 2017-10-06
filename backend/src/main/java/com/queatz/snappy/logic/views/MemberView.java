package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 4/9/17.
 */

public class MemberView extends LinkView {
    final String status;
    final String role;

    public MemberView(EarthAs as, EarthThing member) {
        this(as, member, EarthView.DEEP);
    }

    public MemberView(EarthAs as, EarthThing member, EarthView view) {
        super(as, member, view);
        status = member.getString(EarthField.STATUS);

        if (member.has(EarthField.ROLE)) {
            role = member.getString(EarthField.ROLE);
        } else {
            role = null;
        }
    }
}
