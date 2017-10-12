package com.village.things;

import com.queatz.snappy.queue.UrlPostQueueThread;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.util.HttpUtil;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 10/11/17.
 */

public class ActionWorker extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getParameter(Config.PARAM_URL);

        try {
            String response = HttpUtil.get(url);
            Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue error: " + e);
        }
    }
}
