package com.queatz.snappy.plugins;

import com.queatz.earth.EarthThing;

public interface MemberEditorPlugin {
    EarthThing create(EarthThing source, EarthThing target, String status);
    EarthThing create(EarthThing source, EarthThing target, String status, String role);
}
