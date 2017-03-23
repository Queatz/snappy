package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthRef;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by jacob on 3/20/17.
 */

public class DeviceMine extends EarthControl {
    public DeviceMine(@NotNull EarthAs as) {
        super(as);
    }
    
    public List<EarthThing> forUser(String userId) {
        return use(EarthStore.class).find(
                EarthKind.DEVICE_KIND,
                EarthField.SOURCE,
                new EarthRef(userId)
        );
    }

    public EarthThing forUserAndDevice(String userId, String deviceId) {
        List<EarthThing> devices = use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                "x." + EarthField.SOURCE + " == @source and " +
                "x." + "regId" + " == @regId",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.DEVICE_KIND,
                        "source", userId,
                        "regId", deviceId
                )
        );

        if (devices.isEmpty()) {
            return null;
        } else {
            return devices.get(0);
        }
    }
}
