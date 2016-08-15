package com.queatz.snappy.team;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.JsonObject;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.SuccessResponseSpec;
import com.queatz.snappy.util.Json;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by jacob on 3/27/15.
 */
public class Buy {
    public interface PurchaseCallback {
        void onSuccess();
        void onError();
    }

    public interface OnAttachedCallback {
        void onAttached();
    }

    public Team team;
    private ServiceConnection serviceConnection = null;
    private IInAppBillingService billingService = null;
    private boolean mPlayServicesAvailable = false;
    private String mGooglePurchaseData = null;
    private String mHostingEnabled = null;

    final private HashSet<PurchaseCallback> mPurchaseCallbacks = new HashSet<>();

    public Buy(Team t) {
        team = t;
    }

    public String hostingEnabled() {
        // XXX TODO remove buy stuff
        if (true) {
            return Config.HOSTING_ENABLED_TRUE;
        }

        if(mHostingEnabled == null) {
            mHostingEnabled = team.preferences.getString(Config.PREFERENCE_HOSTING_ENABLED, null);
        }

        return mHostingEnabled;
    }

    public boolean bought() {
        return mGooglePurchaseData != null;
    }

    public JsonObject getPurchaseData() {
        if(mGooglePurchaseData == null)
            return null;

        return Json.from(mGooglePurchaseData, JsonObject.class);
    }

    public void callback(PurchaseCallback callback) {
        mPurchaseCallbacks.add(callback);
    }

    public void pullPerson() {
        team.api.get(Config.PATH_EARTH + "/" + Config.PATH_ME_BUY, new Api.Callback() {
            @Override
            public void success(String response) {
                if(response == null) {
                    return;
                }

                SuccessResponseSpec success = Json.from(response, SuccessResponseSpec.class);

                if (success == null) {
                    return;
                }

                response = success.success;

                boolean purchased = false;

                if(Config.HOSTING_ENABLED_FALSE.equals(response)) {

                }
                else if(Config.HOSTING_ENABLED_AVAILABLE.equals(response)) {

                }
                else if(Config.HOSTING_ENABLED_TRUE.equals(response)) {
                    purchased = true;
                    mGooglePurchaseData = response;
                }

                mHostingEnabled = response;
                team.preferences.edit().putString(Config.PREFERENCE_HOSTING_ENABLED, mHostingEnabled).apply();

                callbacks(purchased);
            }

            @Override
            public void fail(String response) {
                callbacks(false);
            }
        });
    }

    public void pullGoogle(final Activity activity) {
        attach(activity, new OnAttachedCallback() {
            @Override
            public void onAttached() {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        if(billingService == null)
                            return null;

                        try {
                            Bundle activeSubs = billingService.getPurchases(3, team.context.getPackageName(), "subs", Config.BILLING_INAPP_CONTINUATION_TOKEN);

                            if (activeSubs.getInt(Config.BILLING_RESPONSE_CODE) == Config.BILLING_RESPONSE_RESULT_OK) {
                                ArrayList<String> purchases = activeSubs.getStringArrayList(Config.BILLING_INAPP_PURCHASE_ITEM_LIST);
                                ArrayList<String> data = activeSubs.getStringArrayList(Config.BILLING_INAPP_PURCHASE_DATA_LIST);

                                for (int i = 0; i < purchases.size(); i++) {
                                    String purchase = purchases.get(i);
                                    if (Config.subscriptionProductId.equals(purchase)) {
                                        mGooglePurchaseData = data.get(i);
                                        send(mGooglePurchaseData);
                                        callbacks(mGooglePurchaseData != null);
                                    }
                                }
                            }
                        }
                        catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                }.execute();
            }
        });
    }

    public void attach(final Activity activity, final OnAttachedCallback onAttachedCallback) {
        if(!mPlayServicesAvailable)
            checkPlayServices(activity);

        if(billingService != null) {
            onAttachedCallback.onAttached();
        }

        if(serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    billingService = null;
                }

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    billingService = IInAppBillingService.Stub.asInterface(service);
                    if(onAttachedCallback != null) {
                        onAttachedCallback.onAttached();
                    }
                }
            };
        }

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        activity.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void buy(final Activity activity) {
        attach(activity, new OnAttachedCallback() {
            @Override
            public void onAttached() {
                Bundle buyIntentBundle;

                try {
                    buyIntentBundle = billingService.getBuyIntent(3, team.context.getPackageName(), Config.subscriptionProductId, "subs", "<3");
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                    callbacks(false);
                    return;
                }

                int result = buyIntentBundle.getInt(Config.BILLING_RESPONSE_CODE);

                if (result == Config.BILLING_RESPONSE_RESULT_OK) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable(Config.BILLING_BUY_INTENT);

                    try {
                        activity.startIntentSenderForResult(pendingIntent.getIntentSender(), Config.REQUEST_CODE_BUY_INTENT, new Intent(), 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                        callbacks(false);
                    }
                }
            }
        });
    }

    private boolean checkPlayServices(final Activity activity) {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                GooglePlayServicesUtil.getErrorDialog(status, activity, Config.REQUEST_CODE_PLAY_SERVICES).show();
            }
            return false;
        }

        mPlayServicesAvailable = true;
        return true;
    }

    public void onActivityResult(final Activity activity, int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.REQUEST_CODE_BUY_INTENT:
                if(resultCode == Activity.RESULT_OK) {
                    final int responseCode = data.getIntExtra(Config.BILLING_RESPONSE_CODE, 0);

                    if(responseCode == Config.BILLING_RESPONSE_RESULT_OK) {
                        final String purchaseData = data.getStringExtra(Config.BILLING_INAPP_PURCHASE_DATA);
                        final String dataSignature = data.getStringExtra(Config.BILLING_INAPP_DATA_SIGNATURE);

                        mGooglePurchaseData = purchaseData;
                        send(mGooglePurchaseData);
                        callbacks(mGooglePurchaseData != null);
                    }
                    else if(responseCode == Config.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                        pullGoogle(activity);
                    }
                    else {
                        callbacks(false);
                    }
                }
                else {
                    callbacks(false);
                }
                break;
            case Config.REQUEST_CODE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    mPlayServicesAvailable = true;
                }
                break;
        }
    }

    private void send(String purchaseData) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_PURCHASE_DATA, purchaseData);
        team.api.post(Config.PATH_EARTH + "/" + Config.PATH_ME_BUY, params);
    }

    private void callbacks(boolean succeeded) {
        synchronized (mPurchaseCallbacks) {
            if(succeeded) {
                for (PurchaseCallback purchaseCallback : mPurchaseCallbacks) {
                    purchaseCallback.onSuccess();
                }
            }
            else {
                for (PurchaseCallback purchaseCallback : mPurchaseCallbacks) {
                    purchaseCallback.onError();
                }
            }

            mPurchaseCallbacks.clear();
        }
    }
}
