package com.village.things;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.snappy.api.RequestMethod;
import com.queatz.snappy.queue.UrlPostQueueThread;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.util.HttpUtil;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        String type = req.getParameter(Config.PARAM_TYPE);
        String url = req.getParameter(Config.PARAM_URL);
        String data = req.getParameter(Config.PARAM_DATA);

        try {
            String response;

            if (Strings.isNullOrEmpty(type) || type.equals(RequestMethod.GET.name())) {
                response = HttpUtil.get(url);
            } else {
                List<NameValuePair> params = new ArrayList<>();

                if (!Strings.isNullOrEmpty(data)) {
                    JsonObject jsonObject = new EarthJson().fromJson(data, JsonObject.class);

                    for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                        params.add(new BasicNameValuePair(e.getKey(), e.getValue().getAsString()));
                    }
                }

                response = HttpUtil.post(
                        url,
                        ContentType.create("application/x-www-form-urlencoded", Consts.UTF_8).toString(),
                        params
                );
            }
            Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue error: " + e);
        }
    }
}
