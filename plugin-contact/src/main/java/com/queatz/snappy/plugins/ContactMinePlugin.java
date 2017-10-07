package com.queatz.snappy.plugins;

import com.queatz.earth.EarthThing;

import java.util.List;

public interface ContactMinePlugin {
    List<EarthThing> getContacts(EarthThing thing);
}
