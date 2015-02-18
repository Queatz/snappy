package com.queatz.snappy.team;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

/**
 * Created by jacob on 11/16/14.
 */

public class Api {
    public static interface Callback {
        public void success(String response);
        public void fail(String response);
    }

    private static class ApiCallback extends AsyncHttpResponseHandler {
        Api mApi;
        Callback mCallback;

        public ApiCallback(Api api, Callback callback) {
            mApi = api;
            mCallback = callback;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if(mCallback != null)
                mCallback.success((responseBody == null ? null : new String(responseBody)));

            Log.w(Config.LOG_TAG, "api - success - " + (responseBody == null ? null : new String(responseBody)));
        }

        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            switch (statusCode) {
                case HttpStatus.SC_UNAUTHORIZED:
                    mApi.team.auth.reauth();
            }

            if(mCallback != null)
                mCallback.fail(responseBody == null ? null : new String(responseBody));

            Log.w(Config.LOG_TAG, "api - fail - " + (responseBody == null ? null : new String(responseBody)));
        }
    }

    public Team team;

    private AsyncHttpClient mClient;

    public Api(Team t) {
        team = t;

        mClient = new AsyncHttpClient();
    }

    private RequestParams auth(RequestParams params) {
        if(params == null) {
            params = new RequestParams();
        }
        else if(params.has(Config.PARAM_AUTH))
            return params;

        params.put(Config.PARAM_AUTH, team.auth.getAuthParam());

        return params;
    }

    private String makeUrl(String url) {
        return makeUrl(url, null);
    }

    private String makeUrl(String url, RequestParams params) {
        return Config.API_URL + "/" + url + "?" + auth(params).toString();
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

    public void get(String url, RequestParams params) {
        get(url, params, null);
    }

    public void put(String url, RequestParams params) {
        put(url, params, null);
    }

    public void post(String url, RequestParams params) {
        post(url, params, null);
    }

    public void delete(String url, RequestParams params) {
        delete(url, params, null);
    }

    public void get(String url, RequestParams params, Callback callback) {
        mClient.get(Config.API_URL + "/" + url, auth(params), new ApiCallback(this, callback));
    }

    public void post(String url, RequestParams params, Callback callback) {
        mClient.post(makeUrl(url), params, new ApiCallback(this, callback));
    }

    public void put(String url, RequestParams params, Callback callback) {
        mClient.put(makeUrl(url), params, new ApiCallback(this, callback));
    }

    public void delete(String url, RequestParams params, Callback callback) {
        mClient.delete(makeUrl(url, params), new ApiCallback(this, callback));
    }
}