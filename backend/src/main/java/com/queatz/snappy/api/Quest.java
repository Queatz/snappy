package com.queatz.snappy.api;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.QuestSpec;

import java.io.IOException;

/**
 * Created by jacob on 9/15/15.
 */
public class Quest extends Api.Path {
    public Quest(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        switch (method) {
            case GET:
                switch (path.size()) {
                    case 1:
                        getQuest(path.get(0));
                        break;
                    default:
                        die("quest - bad path");
                }

                break;
            case POST:
                switch (path.size()) {
                    case 0:
                        post();

                        break;
                    case 1:
                        if (Boolean.valueOf(request.getParameter(Config.PARAM_START))) {
                            postStart(path.get(0));
                        } else if (Boolean.valueOf(request.getParameter(Config.PARAM_COMPLETE))) {
                            postComplete(path.get(0));
                        }

                        break;
                    default:
                        die("quest - bad path");
                }

                break;
            case DELETE:
                switch (path.size()) {
                    case 1:
                        delete(path.get(0));
                        break;
                    default:
                        die("quest - bad path");
                }

                break;
            default:
                die("quest - bad path");
        }
    }

    private void getQuest(String questId) {
        ok(Datastore.get(QuestSpec.class, questId));
    }

    private void post() {
        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        QuestSpec quest = Thing.getService().quest.createFromRequest(user, request);
        quest.localId = localId;
        ok(quest);
    }

    private void postStart(String questId) {
        QuestSpec quest = Datastore.get(QuestSpec.class, questId);

        if (quest == null) {
            ok(false);
        }

        if (Config.QUEST_STATUS_STARTED.equals(quest.status)) {
            Push.getService().send(Datastore.id(quest.hostId), new PushSpec(Config.PUSH_ACTION_QUEST_STARTED, quest));

            quest.team = Thing.getService().quest.getTeam(quest);

            if (quest.teamSize > 1) {
                for (PersonSpec person : quest.team) {
                    if (!user.id.equals(person.id)) {
                        Push.getService().send(person.id, new PushSpec(Config.PUSH_ACTION_QUEST_STARTED, quest));
                    }
                }
            }
        }

        ok(true);
    }

    private void postComplete(String questId) {
        QuestSpec quest = Datastore.get(QuestSpec.class, questId);

        if (!user.id.equals(Datastore.id(quest.hostId))) {
            die("quest - not authenticated");
        }

        quest = Thing.getService().quest.setQuestStatus(quest, Config.QUEST_STATUS_COMPLETE);

        if (quest != null) {
            quest.team = Thing.getService().quest.getTeam(quest);

            for (PersonSpec person : quest.team) {
                Push.getService().send(person.id, new PushSpec(Config.PUSH_ACTION_QUEST_COMPLETED, quest));
            }
        }

        ok(quest != null);
    }

    private void delete(String questId) throws IOException {
        ok(Thing.getService().quest.delete(user.id, questId));
    }
}
