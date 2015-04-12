package com.queatz.snappy;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.backend.RegistrationRecord;
import com.queatz.snappy.service.Search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.print.Doc;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.queatz.snappy.backend.OfyService.ofy;

/**
 * Created by jacob on 4/11/15.
 */
public class Worker extends HttpServlet {
    private static final String API_KEY = System.getProperty("gcm.api.key");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String action = req.getParameter("action");

        switch (action) {
            case "message":
                String toUser = req.getParameter("toUser");
                String fromUser = req.getParameter("fromUser");
                String message = req.getParameter("message");

                Sender sender = new Sender(API_KEY);
                Message msg = new Message.Builder().addData("message", message).build();

                ArrayList<String> toUsers = new ArrayList<>();

                if(toUser != null) {
                    toUsers.add(toUser);
                }

                if(fromUser != null) {
                    Results<ScoredDocument> results = Search.getService().index.get(Search.Type.FOLLOW).search("following = \"" + fromUser + "\"");

                    if(results != null) {
                        for(Document follow : results) {
                            toUsers.add(follow.getOnlyField("person").getAtom());
                        }
                    }
                }

                for(String userId : toUsers) {
                    List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).filter("userId", userId).list();
                    for (RegistrationRecord record : records) {
                        try {
                            Result result = sender.send(msg, record.getRegId(), 5);

                            if (result.getMessageId() != null) {
                                String canonicalRegId = result.getCanonicalRegistrationId();
                                if (canonicalRegId != null) {
                                    record.setRegId(canonicalRegId);
                                    ofy().save().entity(record).now();
                                }
                            } else {
                                String error = result.getErrorCodeName();
                                if (error.equals(Constants.ERROR_NOT_REGISTERED) || error.equals(Constants.ERROR_MISMATCH_SENDER_ID)) {
                                    ofy().delete().entity(record).now();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
}
