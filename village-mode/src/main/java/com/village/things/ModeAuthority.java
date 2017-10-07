package com.village.things;

import com.queatz.earth.Authority;
import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 10/7/17.
 */

public class ModeAuthority implements Authority {
    @Override
    public boolean authorize(EarthThing as, @NotNull EarthThing entity, @NotNull EarthRule rule) {
        switch (rule) {
            case ACCESS:
                return true;
            case MODIFY:
                return as == null;
        }

        return false;
    }
}
