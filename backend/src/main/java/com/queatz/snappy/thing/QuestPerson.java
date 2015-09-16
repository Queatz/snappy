package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jacob on 9/16/15.
 */
public class QuestPerson implements Thing {
    @Override
    public JSONObject toJson(Document d, String user, boolean shallow) {
        JSONObject jsonObject = new JSONObject();

        Document person = Search.getService().get(Search.Type.PERSON, d.getOnlyField("person").getAtom());
        Document quest = Search.getService().get(Search.Type.QUEST, d.getOnlyField("quest").getAtom());

        try {
            jsonObject.put("person", Things.getService().person.toJson(person, user, true));
            jsonObject.put("quest", Things.getService().quest.toJson(quest, user, true));

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
