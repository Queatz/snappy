package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.QuestLinkSpec;
import com.queatz.snappy.shared.things.QuestSpec;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 9/15/15.
 */
public class Quest {
    public QuestSpec createFromRequest(PersonSpec user, HttpServletRequest request) {
        int teamSize = 1;

        try {
            teamSize = Integer.parseInt(request.getParameter(Config.PARAM_TEAM_SIZE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if(teamSize < 1) {
            teamSize = 1;
        } else if(teamSize > Config.QUEST_MAX_TEAM_SIZE) {
            teamSize = Config.QUEST_MAX_TEAM_SIZE;
        }

        QuestSpec quest = Datastore.create(QuestSpec.class);
        quest.hostId = Datastore.key(user);
        quest.time = request.getParameter(Config.PARAM_TIME);
        quest.status = Config.QUEST_STATUS_OPEN;
        quest.reward = request.getParameter(Config.PARAM_REWARD);
        quest.name = request.getParameter(Config.PARAM_NAME);
        quest.details = request.getParameter(Config.PARAM_DETAILS);
        quest.teamSize = teamSize;
        quest.opened = new Date();
        quest.latlng = user.latlng;
        Datastore.save(quest);

        return quest;
    }

    public QuestSpec start(String user, String questId) {
        QuestSpec quest = Datastore.get(QuestSpec.class, questId);

        if(quest == null) {
            return null;
        }

        QuestLinkSpec link = Datastore.get(QuestLinkSpec.class).filter("questId", questId).filter("personId", user).first().now();

        if (link != null) {
            return quest;
        }

        int soFar = teamSizeSoFar(quest);

        if (soFar >= quest.teamSize) {
            return null;
        }

        link = Datastore.create(QuestLinkSpec.class);
        link.personId = Datastore.key(PersonSpec.class, user);
        link.questId = Datastore.key(QuestSpec.class, questId);
        Datastore.save(link);

        quest = updateQuestStatus(soFar + 1, quest);

        return quest;
    }

    public boolean delete(String user, String questId) {
        QuestSpec quest = Datastore.get(QuestSpec.class, questId);

        if (quest == null || !user.equals(Datastore.id(quest.hostId))) {
            return false;
        }

        if (teamSizeSoFar(quest) > 0) {
            return false;
        }

        Datastore.delete(quest);
        return true;
    }

    public List<PersonSpec> getTeam(QuestSpec quest) {
        List<PersonSpec> team = new ArrayList<>();

        for(QuestLinkSpec link : Datastore.get(QuestLinkSpec.class).filter("questId", quest.id).list()) {
            team.add(Datastore.get(PersonSpec.class, Datastore.id(link.personId)));
        }

        return team;
    }

    private int teamSizeSoFar(QuestSpec quest) {
        return Datastore.get(QuestLinkSpec.class).filter("questId", quest.id).count();
    }

    public QuestSpec setQuestStatus(QuestSpec quest, String status) {
        quest.status = status;
        Datastore.save(quest);
        return quest;
    }

    private QuestSpec updateQuestStatus(int soFar, QuestSpec quest) {
        if (soFar >= quest.teamSize && Config.QUEST_STATUS_OPEN.equals(quest.status)) {
            quest.status = Config.QUEST_STATUS_STARTED;
            Datastore.save(quest);
        }

        return quest;
    }
}