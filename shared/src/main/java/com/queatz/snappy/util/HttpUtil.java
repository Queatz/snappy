package com.queatz.snappy.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.http.Consts.UTF_8;

/**
 * Created by jacob on 3/14/17.
 */

public class HttpUtil {
    public static String get(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        request.addHeader("Content-Type", "application/json; charset=UTF-8");
        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

        return IOUtils.toString(rd);
    }

    public static String post(String url, String contentType, List<NameValuePair> params) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);

        if (params != null) {
            request.setEntity(new UrlEncodedFormEntity(params));
        }

        request.addHeader("Content-Type", contentType);
        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

        return IOUtils.toString(rd);
    }

    public static String post(String url, String contentType, byte[] payload, String authorization) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);

        if (payload != null) {
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(payload));
            request.setEntity(entity);
        }

        request.addHeader("Content-Type", contentType);

        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

        return IOUtils.toString(rd);
    }

    public static String mapToParametersString(Map<String, String> params) {
        List<NameValuePair> pairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return URLEncodedUtils.format(pairs, UTF_8);
    }

    public static List<NameValuePair> mapToParameters(Map<String, String> params) {
        List<NameValuePair> pairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return pairs;
    }
}
