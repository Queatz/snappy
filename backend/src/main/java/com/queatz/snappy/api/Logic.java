package com.queatz.snappy.api;

import com.queatz.snappy.logic.Earth;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * The main entry point for all earthly access.
 *
 * Created by jacob on 4/2/16.
 */
public class Logic extends Path {

    public Logic(Api api) {
        super(api);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void call() throws IOException {
        String jsonResponse;

        EarthAs as = new EarthAs(api, request, response, path, user);

        switch (method) {
            case GET:
                jsonResponse = new Earth(as).get(new EarthAs(api, request, response, path, user));

                if (jsonResponse != null) {
                    response.getWriter().write(jsonResponse);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                break;
            case POST:
                jsonResponse = new Earth(as).post(new EarthAs(api, request, response, path, user));

                if (jsonResponse != null) {
                    response.getWriter().write(jsonResponse);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                break;
            default:
                throw new NothingLogicResponse("earth - bad method");
        }
    }
}
