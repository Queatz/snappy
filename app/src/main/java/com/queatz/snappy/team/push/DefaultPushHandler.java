package com.queatz.snappy.team.push;

import com.google.gson.JsonObject;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 10/18/15.
 */
public class DefaultPushHandler extends PushHandler {
    public DefaultPushHandler(Team team) {
        super(team);
    }

    public void got(PushSpec<JsonObject> push) {
        switch (push.action) {
            case Config.PUSH_ACTION_CLEAR_NOTIFICATION:
                String n = push.body.get("notification").getAsString();
                team.push.clear(n, false);

                break;
            case Config.PUSH_ACTION_REFRESH_ME:
                team.buy.pullPerson();

        }
    }
}
