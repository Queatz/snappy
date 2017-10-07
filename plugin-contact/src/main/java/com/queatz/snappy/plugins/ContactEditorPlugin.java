package com.queatz.snappy.plugins;

import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 10/6/17.
 */

public interface ContactEditorPlugin {
    EarthThing newContact(@NotNull EarthThing thing, @NotNull EarthThing person);
}
