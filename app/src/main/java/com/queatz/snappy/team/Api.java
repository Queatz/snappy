package com.queatz.snappy.team;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.TrustSelfSignedStrategy;

/**
 * Created by jacob on 11/16/14.
 */

public class Api {

    public static SSLSocketFactory _ssl;

    public static SSLSocketFactory ssl() {
        if (_ssl == null) {
            try {
                _ssl = new SSLSocketFactory(new TrustSelfSignedStrategy(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | KeyStoreException e) {
                e.printStackTrace();
            }
        }

        return _ssl;
    }

    public interface Callback {
        void success(String response);
        void fail(String response);
    }

    public enum HTTPMethod {
        GET,
        POST,
        HEAD,
        PUT,
        DELETE,
        PATCH,
    }

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
            Log.d(Config.LOG_TAG, "api - success - " + request.url + " - " + request.params + " - " + (responseBody == null ? null : new String(responseBody)));

            if(mCallback != null)
                mCallback.success((responseBody == null ? null : new String(responseBody)));
        }

        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.d(Config.LOG_TAG, "api - fail - " + request.url + " - " + request.params + " - " + (responseBody == null ? null : new String(responseBody)));

            switch (statusCode) {
                case HttpStatus.SC_UNAUTHORIZED:
//                    mApi.team.auth.reauth();
                    break;
            }

            if(statusCode >= 500) {
                if(this.retry())
                    return;
            }

            if(mCallback != null)
                mCallback.fail(responseBody == null ? null : new String(responseBody));
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
        mClient.setTimeout(30000);
        mClient.setSSLSocketFactory(ssl());
    }

    public AsyncHttpClient client() {
        return mClient;
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
        return Config.API_URL + url + (url.contains("?") ? "&" : "?") + auth(params).toString();
    }

    public RequestHandle get(String url) {
        return get(url, null, null);
    }

    public RequestHandle put(String url) {
        return put(url, null, null);
    }

    public RequestHandle post(String url) {
        return post(url, null, null);
    }

    public RequestHandle delete(String url) {
        return delete(url, null, null);
    }

    public RequestHandle get(String url, Callback callback) {
        return get(url, null, callback);
    }

    public RequestHandle put(String url, Callback callback) {
        return put(url, null, callback);
    }

    public RequestHandle post(String url, Callback callback) {
        return post(url, null, callback);
    }

    public RequestHandle delete(String url, Callback callback) {
        return delete(url, null, callback);
    }

    public RequestHandle get(String url, RequestParams params) {
        return get(url, params, null);
    }

    public RequestHandle put(String url, RequestParams params) {
        return put(url, params, null);
    }

    public RequestHandle post(String url, RequestParams params) {
        return post(url, params, null);
    }

    public RequestHandle delete(String url, RequestParams params) {
        return delete(url, params, null);
    }

    public RequestHandle get(String url, RequestParams params, Callback callback) {
        RequestObject request = new RequestObject(HTTPMethod.GET, Config.API_URL + url, auth(params));
        return mClient.get(request.url, request.params, new ApiCallback(this, request, callback));
    }

    public RequestHandle post(String url, RequestParams params, Callback callback) {
        RequestObject request = new RequestObject(HTTPMethod.POST, makeUrl(url), params);
        return mClient.post(request.url, request.params, new ApiCallback(this, request, callback));
    }

    public RequestHandle put(String url, RequestParams params, Callback callback) {
        RequestObject request = new RequestObject(HTTPMethod.PUT, makeUrl(url), params);
        return mClient.put(request.url, request.params, new ApiCallback(this, request, callback));
    }

    public RequestHandle delete(String url, RequestParams params, Callback callback) {
        RequestObject request = new RequestObject(HTTPMethod.DELETE, makeUrl(url, params), null);
       return  mClient.delete(request.url, new ApiCallback(this, request, callback));
    }
}