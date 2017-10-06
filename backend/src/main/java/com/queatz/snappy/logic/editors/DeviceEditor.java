package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.mines.DeviceMine;

import java.util.Date;

/**
 * Created by jacob on 3/20/17.
 */

public class DeviceEditor extends EarthControl {
    private final EarthStore earthStore;

    public DeviceEditor(EarthAs as) {
        super(as);
        earthStore = use(EarthStore.class);
    }

    public EarthThing newDevice(String userId, String deviceId, String socialMode) {
        EarthThing device = use(DeviceMine.class).forUserAndDevice(userId, deviceId);

        if (device != null) {
            EarthThing.Builder edit = earthStore.edit(device)
                    .set("socialMode", socialMode)
                    .set("updated", new Date());

            device = earthStore.save(edit);
        }
        else {
            device = earthStore.create(EarthKind.DEVICE_KIND);
            EarthThing.Builder edit = earthStore.edit(device)
                    .set("regId", deviceId)
                    .set(EarthField.SOURCE, userId)
                    .set("socialMode", socialMode)
                    .set("updated", new Date())
                    .set("created", new Date());

            device = earthStore.save(edit);
        }

        return device;
    }

    public void remove(EarthThing device) {
        earthStore.conclude(device);
    }

    public void setRegId(EarthThing device, String regId) {
        earthStore.save(earthStore.edit(device).set("regId", regId));
    }

    public void removeFor(String userId, String deviceId) {
        EarthThing device = use(DeviceMine.class).forUserAndDevice(userId, deviceId);

        if (device != null) {
            earthStore.conclude(device);
        }
    }
}
