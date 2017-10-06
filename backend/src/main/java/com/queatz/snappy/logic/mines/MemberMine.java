package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by jacob on 4/9/17.
 */

public class MemberMine extends EarthControl {
    public MemberMine(@NotNull EarthAs as) {
        super(as);
    }

    public List<EarthThing> byThingWithStatus(EarthThing thing, String status) {
        return use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.TARGET + " == @target and " +
                        "x." + EarthField.STATUS + " == @status",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MEMBER_KIND,
                        "target", thing.key().name(),
                        "status", status
                ));
    }

    public EarthThing byThingToThing(EarthThing source, EarthThing target) {
        List<EarthThing> things = use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.TARGET + " == @target and " +
                        "x." + EarthField.SOURCE + " == @source",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MEMBER_KIND,
                        "target", target.key().name(),
                        "source", source.key().name()
                ), 1);

        if (things.isEmpty()) {
            return null;
        } else {
            return things.get(0);
        }
    }

    public List<EarthThing> isAMemberOfThingsWithStatus(EarthThing thing, String status) {
        return use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.SOURCE + " == @source and " +
                        "x." + EarthField.STATUS + " == @status",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MEMBER_KIND,
                        "source", thing.key().name(),
                        "status", status
                ));
    }

    public List<EarthThing> isAMemberOfThingsWithStatus(EarthThing thing, String filterByKind, String status) {
        return use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.SOURCE + " == @source and " +
                        "x." + EarthField.STATUS + " == @status and " +
                        "(for y in " + EarthStore.DEFAULT_COLLECTION + " filter y._key == x." + EarthField.TARGET + " return y." + EarthField.KIND + ")[0] == @memberKind",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.MEMBER_KIND,
                        "source", thing.key().name(),
                        "status", status,
                        "memberKind", filterByKind
                ));
    }
}
