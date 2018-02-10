package com.village.things;

import com.google.common.base.Strings;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.queatz.earth.EarthQueries;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.earth.EarthVisibility;
import com.queatz.earth.FrozenQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.plugins.MemberEditorPlugin;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.vlllage.graph.EarthGraph;

import java.util.Map;

/**
 * Created by jacob on 10/23/17.
 */

public abstract class ExistenceInterface implements Interfaceable {


    protected void isIn(EarthAs as, EarthThing thing, String in) {
        if (!Strings.isNullOrEmpty(in)) {
            EarthThing of = as.s(EarthStore.class).get(in);

            isIn(as, thing, of);
        }
    }

    protected void isIn(EarthAs as, EarthThing thing, EarthThing of) {
        if (of != null) {
            // TODO: Make suggestion if not owned by me
            as.s(MemberEditorPlugin.class).create(thing, of, Config.MEMBER_STATUS_ACTIVE);
        } else {
            // Silent fail
        }
    }

    // Common visibility
    protected void setVisibility(EarthAs as, EarthThing thing) {
        String hidden = extract(as.getParameters().get(Config.PARAM_HIDDEN));

        if (hidden != null) {
            setVisibilityHidden(as, thing, hidden);
        }

        String clubs = extract(as.getParameters().get(Config.PARAM_CLUBS));

        if (clubs != null) {
            setVisibilityClubs(as, thing, clubs);
        }
    }

    protected void setVisibilityClubs(EarthAs as, EarthThing thing, String clubs) {
        try {
            Map<String, Boolean> clubsMap = as.s(EarthJson.class).fromJson(
                    clubs,
                    new TypeToken<Map<String, Boolean>>() {}.getType()
            );

            as.s(EarthVisibility.class).setVisibility(thing, clubsMap);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    protected void setVisibilityHidden(EarthAs as, EarthThing thing, String hidden) {
        as.s(EarthVisibility.class).setHidden(thing, Boolean.parseBoolean(hidden));
    }

    protected String returnIfGraph(EarthAs as, EarthThing thing) {
        String select = extract(as.getParameters().get(Config.PARAM_SELECT));
        FrozenQuery query = as.s(EarthQueries.class).byId(thing.key().name());

        return as.s(EarthJson.class).toJson(
                as.s(EarthGraph.class).queryOne(query.getEarthQuery(), select, query.getVars())
        );
    }


    protected String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }
}
