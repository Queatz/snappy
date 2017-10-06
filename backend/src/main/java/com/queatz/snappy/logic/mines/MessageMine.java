package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.api.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.shared.earth.EarthRef;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;

import java.util.List;

/**
 * Created by jacob on 5/15/16.
 */
public class MessageMine extends EarthControl {
    public MessageMine(final EarthAs as) {
        super(as);
    }

    public List<EarthThing> messagesFromTo(EarthRef source, EarthRef target) {
        return use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.SOURCE + " == @source and " +
                        "x." + EarthField.TARGET + " == @target",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MESSAGE_KIND,
                        "source", source.name(),
                        "target", target.name()
                ));
    }

    public List<EarthThing> messagesFrom(EarthRef source) {
        return use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.SOURCE + " == @source",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MESSAGE_KIND,
                        "source", source.name()
                ));
    }

    public List<EarthThing> messagesTo(EarthRef target) {
        return use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.TARGET + " == @target",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MESSAGE_KIND,
                        "target", target.name()
                ));
    }

    public List<EarthThing> messagesFromOrTo(EarthRef key) {
        return use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and (" +
                        "x." + EarthField.SOURCE + " == @key or " +
                        "x." + EarthField.TARGET + " == @key)",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MESSAGE_KIND,
                        "key", key.name()
                ));
    }
}
