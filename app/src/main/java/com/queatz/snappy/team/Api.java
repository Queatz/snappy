package com.queatz.snappy.team;

import android.os.Handler;
import android.os.Looper;
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

    public enum HTTPMethod {
        GET,
        POST,
        HEAD,
        PUT,
        DELETE,
        PATCH,
    };

    private static class RequestObject {
        public HTTPMethod method;
        public String url;
        public RequestParams params;

        public RequestObject(HTTPMethod method, String url, RequestParams params) {
            this.method = method;
            this.url = url;
            this.params = params;
        }
    }

    private static class ApiCallback extends AsyncHttpResponseHandler {
        Api mApi;
        Callback mCallback;
        RequestObject request;
        int retry;

        public ApiCallback(Api api, RequestObject req, Callback callback) {
            mApi = api;
            mCallback = callback;
            retry = 0;
            request = req;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if(mCallback != null)
                mCallback.success((responseBody == null ? null : new String(responseBody)));

            Log.d(Config.LOG_TAG, "api - success - " + request.url + " - " + request.params + " - " + (responseBody == null ? null : new String(responseBody)));
        }

        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            switch (statusCode) {
                case HttpStatus.SC_UNAUTHORIZED:
                    mApi.team.auth.reauth();
                    break;
            }

            if(statusCode >= 500) {
                if(this.retry())
                    return;
            }

            if(mCallback != null)
                mCallback.fail(responseBody == null ? null : new String(responseBody));

            Log.d(Config.LOG_TAG, "api - fail - " + request.url + " - " + request.params + " - " + (responseBody == null ? null : new String(responseBody)));
        }

        @Override
        public boolean getUseSynchronousMode() {
            return false;
        }

        private boolean retry() {
            if(retry >= Config.maxRequestRetries) {
                return false;
            }

            final ApiCallback callback = this;
            Runnable runnable;

            switch(request.method) {
                case GET:
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            mApi.mClient.get(request.url, request.params, callback);
                        }
                    };
                    break;
                case POST:
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            mApi.mClient.post(request.url, request.params, callback);
                        }
                    };
                    break;
                case PUT:
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            mApi.mClient.put(request.url, request.params, callback);
                        }
                    };
                    break;
                case DELETE:
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            mApi.mClient.delete(request.url, callback);
                        }
                    };
                    break;
                default:
                    return false;
            }

            retry++;

            long time = (long) (1000 * (Math.pow((double) retry, 2) + Math.random()));

            mApi.mHandler.postDelayed(runnable, time);

            return true;
        }
    }

    public Team team;

    private AsyncHttpClient mClient;
    private Handler mHandler;

    public Api(Team t) {
        team = t;

        mHandler = new Handler(Looper.getMainLooper());
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

    public AsyncHttpClient getInternalClient() {
        return mClient;
    }

    private String makeUrl(String url) {
        return makeUrl(url, null);
    }

    private String makeUrl(String url, RequestParams params) {
        return Config.API_URL + url + "?" + auth(params).toString();
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
        RequestObject request = new RequestObject(HTTPMethod.GET, Config.API_URL + url, auth(params));
        mClient.get(request.url, request.params, new ApiCallback(this, request, callback));
    }

    public void post(String url, RequestParams params, Callback callback) {
        RequestObject request = new RequestObject(HTTPMethod.POST, makeUrl(url), params);
        mClient.post(request.url, request.params, new ApiCallback(this, request, callback));
    }

    public void put(String url, RequestParams params, Callback callback) {
        RequestObject request = new RequestObject(HTTPMethod.PUT, makeUrl(url), params);
        mClient.put(request.url, request.params, new ApiCallback(this, request, callback));
    }

    public void delete(String url, RequestParams params, Callback callback) {
        RequestObject request = new RequestObject(HTTPMethod.DELETE, makeUrl(url, params), null);
        mClient.delete(request.url, new ApiCallback(this, request, callback));
    }
}