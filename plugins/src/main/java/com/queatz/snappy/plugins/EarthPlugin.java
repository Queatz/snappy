package com.queatz.snappy.plugins;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EarthPlugin {

    private static final Map<Class, Class> pluginMapping = new HashMap<>();

    public static <T> void register(@NotNull Class<T> plugin, @NotNull Class<? extends T> impl) {
        if (!plugin.isInterface()) {
            throw new RuntimeException("Cannot install plugin (it should be an interface): " + plugin + " impl: " + impl);
        }

        if (impl.isInterface()) {
            throw new RuntimeException("Cannot install plugin (implementation should not be an interface): " + plugin + " impl: " + impl);
        }

        if (pluginMapping.containsKey(plugin)) {
            throw new RuntimeException("Cannot replace plugin: " + plugin + " impl: " + impl);
        }

        pluginMapping.put(plugin, impl);
    }

    @NotNull
    public static Class plugin(@NotNull Class plugin) {
        if (!pluginMapping.containsKey(plugin)) {
            throw new RuntimeException("Plugin not supported: " + plugin);
        }

        return pluginMapping.get(plugin);
    }
}
