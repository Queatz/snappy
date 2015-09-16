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

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by jacob on 4/11/15.
 */
public class Admin extends Api.Path {
    public Admin(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case GET:
                if (path.size() == 2) {
                    String personEmail = path.get(1);

                    switch (path.get(0)) {
                        case Config.HOSTING_BETATESTER:
                            getBetatester(personEmail);
                            break;
                        case "enable_hosting":
                            getEnableHosting(personEmail);
                            break;
                        case "disable_hosting":
                            getDisableHosting(personEmail);
                            break;
                    }
                }
                break;
        }
    }

    private void getBetatester(String personEmail) throws IOException {
        Document person = null;

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PERSON).search("email = \"" + personEmail + "\"");

        Iterator<ScoredDocument> resultsIterator = results.iterator();

        if (resultsIterator.hasNext()) {
            person = resultsIterator.next();
        }

        if (person != null) {
            String subs = null;

            try {
                subs = person.getOnlyField("subscription").getAtom();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            if (subs == null || subs.isEmpty()) {
                Things.getService().person.updateSubscription(person, Config.HOSTING_BETATESTER);
                Push.getService().send(person.getId(), Util.makeSimplePush(Config.PUSH_ACTION_REFRESH_ME));
                response.getWriter().write(person.getOnlyField("email").getAtom() + " has been upgraded");
            } else {
                response.getWriter().write(person.getOnlyField("email").getAtom() + " is already upgraded");
            }
        }
    }

    private void getEnableHosting(String personEmail) throws IOException {
        Document person = null;

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PERSON).search("email = \"" + personEmail + "\"");

        Iterator<ScoredDocument> resultsIterator = results.iterator();

        if (resultsIterator.hasNext()) {
            person = resultsIterator.next();
        }

        if (person != null) {
            String subs = null;

            try {
                subs = person.getOnlyField("subscription").getAtom();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            if (subs == null || subs.isEmpty()) {
                Things.getService().person.updateSubscription(person, Config.HOSTING_ENABLED_AVAILABLE);
                Push.getService().send(person.getId(), Util.makeSimplePush(Config.PUSH_ACTION_REFRESH_ME));
                response.getWriter().write(person.getOnlyField("email").getAtom() + " can now host");
            } else {
                response.getWriter().write(person.getOnlyField("email").getAtom() + " can already host");
            }
        }
    }

    private void getDisableHosting(String personEmail) throws IOException {
        Document person = null;

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PERSON).search("email = \"" + personEmail + "\"");

        Iterator<ScoredDocument> resultsIterator = results.iterator();

        if (resultsIterator.hasNext()) {
            person = resultsIterator.next();
        }

        if (person != null) {
            String subs = null;

            try {
                subs = person.getOnlyField("subscription").getAtom();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            if (subs == null || subs.isEmpty()) {
                response.getWriter().write(person.getOnlyField("email").getAtom() + " already can't host");
            } else {
                Things.getService().person.updateSubscription(person, "");
                Push.getService().send(person.getId(), Util.makeSimplePush(Config.PUSH_ACTION_REFRESH_ME));
                response.getWriter().write(person.getOnlyField("email").getAtom() + " can no longer host");
            }
        }
    }
}
