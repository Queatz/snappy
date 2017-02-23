package com.queatz.snappy.team.push;

import com.google.gson.JsonObject;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 2/23/17.
 */

public class InformationPushHandler extends PushHandler {
    public InformationPushHandler(Team team) {
        super(team);
    }

    public void got(String action, JsonObject push) {
        switch (action) {
            case Config.PUSH_ACTION_INFORMATION:
                team.auth.sendMe();
        }
    }
}
