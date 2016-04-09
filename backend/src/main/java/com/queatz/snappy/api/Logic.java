package com.queatz.snappy.api;

import com.queatz.snappy.logic.Earth;
import com.queatz.snappy.service.Api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 4/2/16.
 */
public class Logic extends Api.Path {

    public Logic(Api api) {
        super(api);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void call() throws IOException {
        String jsonResponse;

        switch (method) {
            case GET:
                jsonResponse = new Earth().get(path, request.getParameterMap());
                response.getWriter().write(jsonResponse);
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            case POST:
                jsonResponse = new Earth().post(path, request.getParameterMap());
                response.getWriter().write(jsonResponse);
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            default:
                die("logic - bad method");
        }
    }
}
