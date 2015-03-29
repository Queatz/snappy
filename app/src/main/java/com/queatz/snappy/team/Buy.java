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
import com.queatz.snappy.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by jacob on 3/27/15.
 */
public class Buy {
    public static interface PurchaseCallback {
        void onSuccess(JSONObject purchaseData);
        void onError();
    }

    public static interface OnAttachedCallback {
        void onAttached();
    }

    public Team team;
    private ServiceConnection serviceConnection = null;
    private IInAppBillingService billingService = null;
    private boolean mPlayServicesAvailable = false;

    final private HashSet<PurchaseCallback> mPurchaseCallbacks = new HashSet<>();

    public Buy(Team t) {
        team = t;
    }

    public void pull(final Activity activity) {
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

                            if (activeSubs.getInt(Config.BILLING_RESPONSE_CODE) == 0) {
                                ArrayList<String> purchases = activeSubs.getStringArrayList(Config.BILLING_INAPP_PURCHASE_ITEM_LIST);
                                ArrayList<String> data = activeSubs.getStringArrayList(Config.BILLING_INAPP_PURCHASE_DATA_LIST);

                                for (int i = 0; i < purchases.size(); i++) {
                                    String purchase = purchases.get(i);
                                    if (purchase.contains(Config.subscriptionProductId)) {

                                        try {
                                            callbacks(new JSONObject(data.get(i)));
                                        }
                                        catch (JSONException e) {
                                            e.printStackTrace();
                                        }
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

    public void buy(final Activity activity, final PurchaseCallback purchaseCallback) {
        attach(activity, new OnAttachedCallback() {
            @Override
            public void onAttached() {
                mPurchaseCallbacks.add(purchaseCallback);

                Bundle buyIntentBundle;

                try {
                    buyIntentBundle = billingService.getBuyIntent(3, team.context.getPackageName(), Config.subscriptionProductId, "subs", "<3");
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                    callbacks(null);
                    return;
                }

                int result = buyIntentBundle.getInt(Config.BILLING_RESPONSE_CODE);

                if (result == Config.BILLING_RESPONSE_RESULT_OK) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable(Config.BILLING_BUY_INTENT);

                    try {
                        activity.startIntentSenderForResult(pendingIntent.getIntentSender(), Config.REQUEST_CODE_BUY_INTENT, new Intent(), 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                        callbacks(null);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.REQUEST_CODE_BUY_INTENT:
                if(resultCode == Activity.RESULT_OK) {
                    final int responseCode = data.getIntExtra(Config.BILLING_RESPONSE_CODE, 0);

                    if(responseCode == Config.BILLING_RESPONSE_RESULT_OK) {
                        final String purchaseData = data.getStringExtra(Config.BILLING_INAPP_PURCHASE_DATA);
                        final String dataSignature = data.getStringExtra(Config.BILLING_INAPP_DATA_SIGNATURE);

                        try {
                            callbacks(new JSONObject(purchaseData));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbacks(null);
                        }
                    }
                    else {
                        callbacks(null);
                    }
                }
                else {
                    callbacks(null);
                }
                break;
            case Config.REQUEST_CODE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    mPlayServicesAvailable = true;
                }
                break;
        }
    }

    private void callbacks(JSONObject purchaseData) {
        synchronized (mPurchaseCallbacks) {
            if(purchaseData == null) {
                for (PurchaseCallback purchaseCallback : mPurchaseCallbacks) {
                    purchaseCallback.onError();
                }
            }
            else {
                for (PurchaseCallback purchaseCallback : mPurchaseCallbacks) {
                    purchaseCallback.onSuccess(purchaseData);
                }
            }

            mPurchaseCallbacks.clear();
        }
    }
}
