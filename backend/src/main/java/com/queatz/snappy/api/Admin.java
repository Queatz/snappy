package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 4/11/15.
 */
public class Admin implements Api.Path {
    Api api;

    public Admin(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                if (path.size() == 2) {
                    Document person = null;
                    String action = path.get(0);
                    String personEmail = path.get(1);

                    if ("betatester".equals(action)) {
                        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PERSON).search("email = \"" + personEmail + "\"");

                        Iterator<ScoredDocument> resultsIterator = results.iterator();

                        if (resultsIterator.hasNext()) {
                            person = resultsIterator.next();
                        }

                        if (person != null) {
                            String subs = null;

                            try {
                                subs = person.getOnlyField("subscription").getAtom();
                            }
                            catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }

                            if(subs == null || subs.isEmpty()) {
                                Things.getService().person.updateSubscription(person, "betatester");
                                resp.getWriter().write(person.getOnlyField("email").getAtom() + " has been upgraded");
                            }
                            else {
                                resp.getWriter().write(person.getOnlyField("email").getAtom() + " is already upgraded");
                            }
                        }
                    }
                }
                break;
        }
    }
}
