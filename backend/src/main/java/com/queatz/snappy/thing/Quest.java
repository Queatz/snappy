package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 9/15/15.
 */
public class Quest implements Thing {
    @Override
    public JSONObject toJson(Document d, String user, boolean shallow) {
        JSONObject jsonObject = new JSONObject();

        Document person = Search.getService().get(Search.Type.PERSON, d.getOnlyField("host").getAtom());

        try {
            jsonObject.put("host", Things.getService().person.toJson(person, user, true));
            jsonObject.put("status", d.getOnlyField("status").getAtom());
            jsonObject.put("details", d.getOnlyField("details").getText());
            jsonObject.put("reward", d.getOnlyField("reward").getText());
            jsonObject.put("opened", Util.dateToString(d.getOnlyField("opened").getDate()));
            jsonObject.put("time", d.getOnlyField("time").getAtom());
            jsonObject.put("teamSize", d.getOnlyField("teamSize").getNumber().intValue());

            if (shallow) {
                return jsonObject;
            }

            Results<ScoredDocument> results = Search.getService().index.get(Search.Type.QUEST_PERSON).search("quest = " + d.getId());

            if (results.getNumberReturned() > 0) {
                JSONArray team = new JSONArray();

                for (ScoredDocument document : results) {
                    team.put(Things.getService().person.toJson(document, user, true));
                }

                if(team.length() > 0)
                    jsonObject.put("team", team);
            }

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document createFromRequest(String user, HttpServletRequest request) {
        int teamSize = 1;

        try {
            teamSize = Integer.getInteger(request.getParameter(Config.PARAM_TEAM_SIZE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if(teamSize < 1) {
            teamSize = 1;
        } else if(teamSize > Config.QUEST_MAX_TEAM_SIZE) {
            teamSize = Config.QUEST_MAX_TEAM_SIZE;
        }

        Document.Builder documentBuilder = Document.newBuilder();
        documentBuilder.addField(Field.newBuilder().setName("host").setAtom(user));
        documentBuilder.addField(Field.newBuilder().setName("time").setAtom(request.getParameter(Config.PARAM_TIME)));
        documentBuilder.addField(Field.newBuilder().setName("status").setAtom(request.getParameter(Config.QUEST_STATUS_OPEN)));
        documentBuilder.addField(Field.newBuilder().setName("reward").setText(request.getParameter(Config.PARAM_REWARD)));
        documentBuilder.addField(Field.newBuilder().setName("details").setText(request.getParameter(Config.PARAM_DETAILS)));
        documentBuilder.addField(Field.newBuilder().setName("teamSize").setNumber(teamSize));
        documentBuilder.addField(Field.newBuilder().setName("opened").setDate(new Date()));

        Document document = documentBuilder.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.QUEST).put(document);
            documentBuilder.setId(put.getIds().get(0));
            return documentBuilder.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document start(String user, String questId) {
        Document quest = Search.getService().get(Search.Type.QUEST, questId);

        if(quest == null) {
            return null;
        }

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.QUEST_PERSON).search("quest = \"" + questId + "\" AND person = \"" + user + "\"");

        if (results.getNumberReturned() > 0) {
            return results.iterator().next();
        }

        if (teamSizeSoFar(quest) >= quest.getOnlyField("teamSize").getNumber().intValue()) {
            return null;
        }

        Document.Builder documentBuilder = Document.newBuilder();
        documentBuilder.addField(Field.newBuilder().setName("person").setAtom(user));
        documentBuilder.addField(Field.newBuilder().setName("quest").setAtom(questId));
        Document questPerson = documentBuilder.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.QUEST_PERSON).put(questPerson);
            documentBuilder.setId(put.getIds().get(0));
            questPerson = documentBuilder.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }

        updateQuestStatus(quest);

        return questPerson;
    }

    public boolean delete(String user, String questId) {
        Document quest = Search.getService().get(Search.Type.QUEST, questId);

        if (quest == null || !user.equals(quest.getOnlyField("host").getAtom())) {
            return false;
        }

        if (teamSizeSoFar(quest) > 0) {
            return false;
        }

        Search.getService().index.get(Search.Type.QUEST).delete(questId);
        return true;
    }

    private int teamSizeSoFar(Document quest) {
        return (int) Search.getService().index.get(Search.Type.QUEST_PERSON).search("quest = \"" + quest.getId() + "\"").getNumberFound();
    }

    private boolean updateQuestStatus(Document quest) {
        long teamSize = quest.getOnlyField("teamSize").getNumber().longValue();
        long soFar = teamSizeSoFar(quest);

        if (teamSize == soFar && Config.QUEST_STATUS_OPEN.equals(quest.getOnlyField("status").getAtom())) {
            Document.Builder documentBulder = Document.newBuilder().setId(quest.getId()).addField(
                    Field.newBuilder().setName("status").setAtom(Config.QUEST_STATUS_STARTED)
            );

            Util.copyIn(documentBulder, quest, "status");

            try {
                Search.getService().index.get(Search.Type.QUEST_PERSON).put(documentBulder.build());
                return true;
            } catch (PutException e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }
}