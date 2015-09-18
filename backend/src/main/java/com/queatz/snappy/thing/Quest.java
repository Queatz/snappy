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
    public JSONObject makePush(Document quest) {
        if(quest == null)
            return null;

        Document person = Search.getService().get(Search.Type.PERSON, quest.getOnlyField("host").getAtom());

        JSONObject push = new JSONObject();

        try {
            String action;

            switch (quest.getOnlyField("status").getAtom()) {
                case Config.QUEST_STATUS_STARTED:
                    action = Config.PUSH_ACTION_QUEST_STARTED;
                    break;
                case Config.QUEST_STATUS_COMPLETE:
                    action = Config.PUSH_ACTION_QUEST_COMPLETED;
                    break;
                default:
                    return null;
            }

            push.put("action", action);
            push.put("quest", toPushJson(quest));
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return push;
    }

    public JSONObject toPushJson(Document d) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("name", d.getOnlyField("name").getText());

            JSONArray team = new JSONArray();
            Results<ScoredDocument> results = getTeam(d);

            for (ScoredDocument document : results) {
                Document questPerson = Search.getService().get(Search.Type.PERSON, document.getOnlyField("person").getAtom());
                team.put(Things.getService().person.toPushJson(questPerson));
            }

            o.put("team", team);

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject toJson(Document d, String user, boolean shallow) {
        JSONObject jsonObject = new JSONObject();

        Document person = Search.getService().get(Search.Type.PERSON, d.getOnlyField("host").getAtom());

        try {
            jsonObject.put("id", d.getId());

            if (shallow) {
                return jsonObject;
            }

            jsonObject.put("host", Things.getService().person.toJson(person, user, true));
            jsonObject.put("status", d.getOnlyField("status").getAtom());
            jsonObject.put("name", d.getOnlyField("name").getText());
            jsonObject.put("details", d.getOnlyField("details").getText());
            jsonObject.put("reward", d.getOnlyField("reward").getText());
            jsonObject.put("opened", Util.dateToString(d.getOnlyField("opened").getDate()));
            jsonObject.put("time", d.getOnlyField("time").getAtom());
            jsonObject.put("teamSize", d.getOnlyField("teamSize").getNumber().intValue());

            Results<ScoredDocument> results = getTeam(d);
            JSONArray team = new JSONArray();

            for (ScoredDocument document : results) {
                Document questPerson = Search.getService().get(Search.Type.PERSON, document.getOnlyField("person").getAtom());
                team.put(Things.getService().person.toJson(questPerson, user, true));
            }

            jsonObject.put("team", team);

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document createFromRequest(String user, HttpServletRequest request) {
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

        Document person = Search.getService().get(Search.Type.PERSON, user);

        Document.Builder documentBuilder = Document.newBuilder();
        documentBuilder.addField(Field.newBuilder().setName("host").setAtom(user));
        documentBuilder.addField(Field.newBuilder().setName("time").setAtom(request.getParameter(Config.PARAM_TIME)));
        documentBuilder.addField(Field.newBuilder().setName("status").setAtom(Config.QUEST_STATUS_OPEN));
        documentBuilder.addField(Field.newBuilder().setName("reward").setText(Util.encode(request.getParameter(Config.PARAM_REWARD))));
        documentBuilder.addField(Field.newBuilder().setName("name").setText(Util.encode(request.getParameter(Config.PARAM_NAME))));
        documentBuilder.addField(Field.newBuilder().setName("details").setText(Util.encode(request.getParameter(Config.PARAM_DETAILS))));
        documentBuilder.addField(Field.newBuilder().setName("teamSize").setNumber(teamSize));
        documentBuilder.addField(Field.newBuilder().setName("opened").setDate(new Date()));
        documentBuilder.addField(Field.newBuilder().setName("latlng").setGeoPoint(person.getOnlyField("latlng").getGeoPoint()));

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

    public Results<ScoredDocument> getTeam(Document quest) {
        return Search.getService().index.get(Search.Type.QUEST_PERSON).search("quest = " + quest.getId());
    }

    private int teamSizeSoFar(Document quest) {
        return (int) Search.getService().index.get(Search.Type.QUEST_PERSON).search("quest = \"" + quest.getId() + "\"").getNumberFound();
    }

    public boolean setQuestStatus(Document quest, String status) {
        if (!status.equals(quest.getOnlyField("status").getAtom())) {
            Document.Builder documentBulder = Document.newBuilder().setId(quest.getId()).addField(
                    Field.newBuilder().setName("status").setAtom(status)
            );

            Util.copyIn(documentBulder, quest, "status");

            try {
                Search.getService().index.get(Search.Type.QUEST).put(documentBulder.build());
                return true;
            } catch (PutException e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    private boolean updateQuestStatus(Document quest) {
        long teamSize = quest.getOnlyField("teamSize").getNumber().longValue();
        long soFar = teamSizeSoFar(quest);

        if (soFar >= teamSize && Config.QUEST_STATUS_OPEN.equals(quest.getOnlyField("status").getAtom())) {
            Document.Builder documentBulder = Document.newBuilder().setId(quest.getId()).addField(
                    Field.newBuilder().setName("status").setAtom(Config.QUEST_STATUS_STARTED)
            );

            Util.copyIn(documentBulder, quest, "status");

            try {
                Search.getService().index.get(Search.Type.QUEST).put(documentBulder.build());
                return true;
            } catch (PutException e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }
}