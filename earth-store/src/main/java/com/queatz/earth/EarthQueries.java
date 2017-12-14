package com.queatz.earth;

import com.google.common.collect.ImmutableMap;
import com.queatz.earth.query.EarthQueryAppendFilter;
import com.queatz.earth.query.EarthQueryNearFilter;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.earth.EarthGeo;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.queatz.earth.EarthStore.CLUB_GRAPH;
import static com.queatz.earth.EarthStore.DEFAULT_FIELD_CREATED;
import static com.queatz.earth.EarthStore.DEFAULT_FIELD_KIND;
import static com.queatz.earth.EarthStore.DEFAULT_GRAPH;
import static com.queatz.earth.EarthStore.DEFAULT_KIND_OWNER;
import static com.queatz.earth.EarthStore.TRANSIENT_KINDS;
import static com.queatz.earth.EarthStore.TRANSIENT_KIND_TIMEOUT_SECONDS;

/**
 * Created by jacob on 11/27/17.
 */

public class EarthQueries extends EarthControl {

    public EarthQueries(@NotNull EarthAs as) {
        super(as);
    }

    public FrozenQuery getNearby(EarthGeo center, String kind, String q) {
        return getNearby(kind, q, center, null);
    }

    public FrozenQuery getNearby(EarthGeo center, String kind, boolean recent, String q) {
        return getNearby(kind, q, center, recent ? new Date(new Date().getTime() - 1000 * 60 * 60) : null);
    }

    public FrozenQuery getNearby(String kind, String q, EarthGeo location, Date afterDate) {
        String filter = "";

        if (afterDate != null) {
            filter += "{thing}." + DEFAULT_FIELD_CREATED + " >= " + (afterDate.getTime());
        }

        if (kind != null) {
            String kinds[] = kind.split(Pattern.quote("|"));

            if (kinds.length > 0 && !kinds[0].isEmpty()) {
                if (!filter.isEmpty()) {
                    filter += " and ";
                }

                filter += "(";

                for (int i = 0; i < kinds.length; i++) {
                    if (i > 0) {
                        filter += " or ";
                    }

                    if (TRANSIENT_KINDS.contains(kinds[i])) {
                        filter += "(";
                        filter += "{thing}.kind == \"" + kinds[i] + "\" ";
                        filter += "and date_timestamp({thing}." + EarthField.AROUND + ") >= date_timestamp(date_subtract(date_now(), " + TRANSIENT_KIND_TIMEOUT_SECONDS + ", 's'))";
                        filter += ")";
                    } else {
                        filter += "{thing}.kind == \"" + kinds[i] + "\"";
                    }
                }

                filter += ")";
            }
        }

        final String Q_PARAM = "q";

        if (q != null) {
            filter += " and " + and(Q_PARAM);
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("latitude", location.getLatitude());
        vars.put("longitude", location.getLongitude());
        vars.put("limit", Config.NEARBY_MAX_COUNT);
        vars.put("owner_kind", DEFAULT_KIND_OWNER);

        if (q != null) {
            vars.put(Q_PARAM, "%" + q.trim().toLowerCase() + "%");
        }

        return new FrozenQuery(
                new EarthQuery(as)
                        .let("things", new EarthQueryNearFilter(as, "@latitude", "@longitude", "@limit").aql())
                        .in(new EarthQueryAppendFilter("things", new EarthQuery(as)
                                .internal()
                                .as("thing")
                                .in("things " + new EarthQuery(as)
                                        .internal()
                                        .as("other, relationship")
                                        .in("outbound thing graph '" + DEFAULT_GRAPH + "'")
                                        .filter("relationship." + DEFAULT_FIELD_KIND, "@owner_kind")
                                        .select("other")
                                        .aql()
                                ).inline().aql()).aql())
                        .filter(EarthField.KIND, "!=", "'" + EarthKind.DEVICE_KIND + "'")
                        .filter(EarthField.KIND, "!=", "'" + EarthKind.GEO_SUBSCRIBE_KIND + "'")
                        .filter(filter)
                        .limit("@limit")
                        .distinct(true),
                vars
        );
    }

    private String and(String q) {
        return "(LOWER({thing}." + EarthField.NAME + ") like @" + q + " or " +
                "LOWER({thing}." + EarthField.FIRST_NAME + ") like @" + q + " or " +
                "LOWER({thing}." + EarthField.LAST_NAME + ") like @" + q + " or " +
                "LOWER({thing}." + EarthField.ABOUT + ") like @" + q + ")";
    }

    public FrozenQuery byId(String id) {
        return new FrozenQuery(
                new EarthQuery(as).filter("_key", "@id").limit("1"),
                ImmutableMap.of(
                        "id", id
                )
        );
    }

    public FrozenQuery messagesFromOrTo(String id) {
        return new FrozenQuery(
                new EarthQuery(as).filter(
                        "{thing}." + EarthField.KIND + " == '" + EarthKind.MESSAGE_KIND + "' and (" +
                                "{thing}." + EarthField.SOURCE + " == @id or " +
                                "{thing}." + EarthField.TARGET + " == @id)")
                .limit("@limit"),
                ImmutableMap.of(
                        "id", id,
                        "limit", Config.NEARBY_MAX_COUNT
                )
        );
    }

    public FrozenQuery messagesFromAndTo(String id, String with) {
        return new FrozenQuery(
                new EarthQuery(as).filter("{thing}." + EarthField.KIND + " == '" + EarthKind.MESSAGE_KIND + "' and " +
                        "(({thing}." + EarthField.SOURCE + " == @source and " +
                        "{thing}." + EarthField.TARGET + " == @target) or " +
                        "({thing}." + EarthField.SOURCE + " == @target and " +
                        "{thing}." + EarthField.TARGET + " == @source))")
                .sort("{thing}." + EarthField.CREATED_ON + " desc")
                .limit("@limit"),
                ImmutableMap.<String, Object>of(
                        "source", id,
                        "target", with,
                        "limit", Config.NEARBY_MAX_COUNT
                ));
    }

    public FrozenQuery recentsFor(String id) {
        return new FrozenQuery(
                new EarthQuery(as).filter("{thing}." + EarthField.KIND + " == '" + EarthKind.RECENT_KIND + "' and " +
                        "{thing}." + EarthField.SOURCE + " == @id")
                .sort("{thing}." + EarthField.CREATED_ON + " desc")
                .limit("@limit"),
                ImmutableMap.of(
                        "id", id,
                        "limit", Config.NEARBY_MAX_COUNT
                )
        );
    }

    public FrozenQuery clubsOf(EarthThing thing) {
        return new FrozenQuery(
                new EarthQuery(as)
                        .in("outbound @id graph '" + CLUB_GRAPH + "'")
                        .filter(EarthField.KIND, "'" + EarthKind.CLUB_KIND + "'")
                        .filter(EarthStore.DEFAULT_FIELD_TO, "!=", "@id")
                        .distinct(true)
                        .sort("{thing}." + EarthField.CREATED_ON + " desc"),
                ImmutableMap.of(
                        "id", thing.id()
                )
        );
    }

    public FrozenQuery byGoogleUrl(String googleUrl) {
        return new FrozenQuery(
                new EarthQuery(as).filter("{thing}." + EarthField.KIND + " == '" + EarthKind.PERSON_KIND + "' and " +
                        "{thing}." + EarthField.GOOGLE_URL + " == @googleUrl").limit("1"),
                ImmutableMap.of(
                        "googleUrl", googleUrl.toLowerCase()
                )
        );
    }
}
