package com.queatz.snappy.team;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;

import org.apache.http.Header;

/**
 * Created by jacob on 11/16/14.
 */

public class Api {
    public static class Callback extends AsyncHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            success(new String(responseBody));
        }

        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            fail(new String(responseBody));

        }

        public void success(String response) {}
        public void fail(String response) {}
    }

    Team team;

    AsyncHttpClient mClient;

    public Api(Team t) {
        team = t;

        mClient = new AsyncHttpClient();
    }

    private RequestParams auth(RequestParams params) {
        if(params == null) {
            params = new RequestParams();
        }

        params.put("auth", team.auth.getAuthParam());

        return params;
    }

    public void get(String url) {
        get(url, null, null);
    }

    public void put(String url) {
        put(url, null, null);
    }

    public void post(String url) {
        post(url, null, null);
    }

    public void delete(String url) {
        delete(url, null, null);
    }

    public void get(String url, Callback callback) {
        get(url, null, callback);
    }

    public void put(String url, Callback callback) {
        put(url, null, callback);
    }

    public void post(String url, Callback callback) {
        post(url, null, callback);
    }

    public void delete(String url, Callback callback) {
        delete(url, null, callback);
    }

    public void get(String url, RequestParams params, Callback callback) {
        if(callback == null) callback = new Callback();

        mClient.get(Config.API_URL + "/" + url, auth(params), callback);
    }

    public void post(String url, RequestParams params, Callback callback) {
        if(callback == null) callback = new Callback();

        mClient.post(Config.API_URL + "/" + url, auth(params), callback);
    }

    public void put(String url, RequestParams params, Callback callback) {
        if(callback == null) callback = new Callback();

        mClient.put(Config.API_URL + "/" + url, auth(params), callback);
    }

    public void delete(String url, RequestParams params, Callback callback) {
        if(callback == null) callback = new Callback();

        mClient.delete(Config.API_URL + "/" + url + "?" + auth(params).toString(), callback);
    }
}