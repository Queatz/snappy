package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;

import javax.print.Doc;

/**
 * Created by jacob on 9/15/15.
 */
public class Quest extends Api.Path {
    public Quest(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
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

    private void getQuest(String questId) throws IOException {
        Document quest = Search.getService().get(Search.Type.QUEST, questId);

        JSONObject json = Things.getService().quest.toJson(quest, user, false);
        response.getWriter().write(json.toString());
    }

    private void post() throws IOException {
        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        Document quest = Things.getService().quest.createFromRequest(user, request);

        JSONObject json = Things.getService().quest.toJson(quest, user, false);
        Util.localId(json, localId);

        response.getWriter().write(json.toString());
    }

    private void postStart(String questId) throws IOException {
        Document quest = Things.getService().quest.start(user, questId);

        if (quest == null) {
            response.getWriter().write(Boolean.toString(false));
            return;
        }

        if (Config.QUEST_STATUS_STARTED.equals(quest.getOnlyField("status").getAtom())) {
            Push.getService().send(quest.getOnlyField("host").getAtom(), Things.getService().quest.makePush(quest));

            Results<ScoredDocument> team = Things.getService().quest.getTeam(quest);

            if (quest.getOnlyField("teamSize").getNumber().intValue() > 1) {
                for (ScoredDocument document : team) {
                    Push.getService().send(document.getOnlyField("person").getAtom(), Things.getService().quest.makePush(quest));
                }
            }
        }

        response.getWriter().write(Boolean.toString(true));
    }

    private void postComplete(String questId) throws IOException, PrintingError {
        Document quest = Search.getService().get(Search.Type.QUEST, questId);

        if (!user.equals(quest.getOnlyField("host").getAtom())) {
            die("quest - not authenticated");
        }

        quest = Things.getService().quest.setQuestStatus(quest, Config.QUEST_STATUS_COMPLETE);

        if (quest != null) {
            for (ScoredDocument document : Things.getService().quest.getTeam(quest)) {
                Push.getService().send(document.getOnlyField("person").getAtom(), Things.getService().quest.makePush(quest));
            }
        }

        response.getWriter().write(Boolean.toString(quest != null));
    }

    private void delete(String questId) throws IOException {
        boolean success = Things.getService().quest.delete(user, questId);
        response.getWriter().write(Boolean.toString(success));
    }
}
