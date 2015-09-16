package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;

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
            case POST:
                switch (path.size()) {
                    case 0:
                        post();

                        break;
                    case 1:
                        if (Boolean.valueOf(request.getParameter(Config.PARAM_START))) {
                            postStart(path.get(0));
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

    private void post() throws IOException {
        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        Document quest = Things.getService().quest.createFromRequest(user, request);

        JSONObject json = Things.getService().quest.toJson(quest, user, false);
        Util.localId(json, localId);

        response.getWriter().write(json.toString());
    }

    private void postStart(String questId) throws IOException {
        Document questPerson = Things.getService().quest.start(user, questId);

        response.getWriter().write(Boolean.toString(questPerson != null));
    }

    private void delete(String questId) throws IOException {
        boolean success = Things.getService().quest.delete(user, questId);
        response.getWriter().write(Boolean.toString(success));
    }
}
