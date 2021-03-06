package com.village.things;

import com.google.common.collect.ImmutableMap;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.shared.earth.EarthRef;

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
