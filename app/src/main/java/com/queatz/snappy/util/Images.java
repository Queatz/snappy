package com.queatz.snappy.util;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import cz.msebera.android.httpclient.conn.ssl.AllowAllHostnameVerifier;
import cz.msebera.android.httpclient.conn.ssl.SSLContextBuilder;
import cz.msebera.android.httpclient.conn.ssl.TrustSelfSignedStrategy;

/**
 * Created by jacob on 8/12/17.
 */

public class Images {

    private static Picasso instance = null;

    public static Picasso with(Context context) {
        if (instance == null) {
            OkHttpClient client = new OkHttpClient();
            client.setHostnameVerifier(new AllowAllHostnameVerifier());
            try {
                client.setSslSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build().getSocketFactory());
                client.setFollowRedirects(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            instance = new Picasso.Builder(context).downloader(new OkHttpDownloader(client)).build();
        }

        return instance;
    }
}
