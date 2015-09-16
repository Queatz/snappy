package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import java.io.IOException;

/**
 * Created by jacob on 9/5/15.
 */
public class Bounty extends Api.Path {
    public Bounty(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case POST:
                if(path.size() != 1) {
                    die("bounty - bad path");
                }

                if(Boolean.valueOf(request.getParameter(Config.PARAM_CLAIM))) {
                    postClaim(path.get(0));
                } else if(Boolean.valueOf(request.getParameter(Config.PARAM_FINISH))) {
                    postFinish(path.get(0));
                }
                else {
                    die("bounty - bad path");
                }

                break;
            case DELETE:
                if(path.size() != 1) {
                    die("bounty - bad path");
                }

                delete(path.get(0));

                break;
            default:
                die("bounty - bad method");
        }
    }

    private void postClaim(String bountyId) throws IOException {
        boolean claimed = Things.getService().bounty.claim(user, bountyId);

        response.getWriter().write(Boolean.toString(claimed));
    }

    private void postFinish(String bountyId) throws IOException {
        Document bounty = Search.getService().get(Search.Type.BOUNTY, bountyId);

        boolean finished = Things.getService().bounty.finish(user, bountyId);

        response.getWriter().write(Boolean.toString(finished));

        if(finished) {
            Push.getService().send(bounty.getOnlyField("poster").getAtom(), Things.getService().bounty.makePush(bounty));
        }
    }

    private void delete(String bountyId) throws IOException {
        Document bounty = Search.getService().get(Search.Type.BOUNTY, bountyId);

        if(bounty != null && user.equals(bounty.getOnlyField("poster").getAtom())) {
            boolean success = Things.getService().bounty.delete(bounty);

            response.getWriter().write(Boolean.toString(success));
        }
    }
}
