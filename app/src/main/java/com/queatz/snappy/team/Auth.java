package com.queatz.snappy.team;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.util.Json;

import java.io.IOException;
import java.util.HashSet;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/19/14.
 */
public class Auth {
    public enum Step {
        AUTHENTICATION_CANCELED,
        AUTHENTICATION_FAILED,
        AUTHENTICATED,
        COMPLETE
    }

    public interface Callback {
        void onStep(Step step);
    }

    private static final String SCOPE = "oauth2: email profile";

    public Team team;
    private String mGoogleAuthToken;
    private String mAuthToken;
    private String mEmail;
    private String mUser;
    private String mGcmRegistrationId;
    private String mSocialMode;
    private GetAuthTokenTask mFetchTask;
    private Activity mActivity;
    private HashSet<Callback> mCallbacks;
    private GcmRegistrationAsyncTask gcmRegistrationAsyncTask = null;
    private Handler mHandler;
    private Runnable mRegisterDeviceRunnable = null;

    private static class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
        private GoogleCloudMessaging gcm;
        private Auth auth;
        private RequestHandle requestHandle = null;

        public GcmRegistrationAsyncTask(Auth auth) {
            this.auth = auth;
        }

        public void cancel() {
            if(requestHandle != null) {
                requestHandle.cancel(true);
            }

            super.cancel(true);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(auth.team.context);
                }

                return gcm.register(Config.PROJECT_ID);
            } catch (IOException e) {
                Log.w(Config.LOG_TAG, "Device registration failed (code 1)");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String regId) {
            auth.setGcmRegistrationId(regId);

            if(regId != null) {
                RequestParams params = new RequestParams();
                params.put(Config.PARAM_DEVICE_ID, regId);
                params.put(Config.PARAM_SOCIAL_MODE, auth.mSocialMode);

                requestHandle = auth.team.api.post(Config.PATH_EARTH + "/" + Config.PATH_ME_REGISTER_DEVICE, params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        Log.w(Config.LOG_TAG, "Device successfully registered, social mode = " + auth.mSocialMode);
                    }

                    @Override
                    public void fail(String response) {
                        Log.w(Config.LOG_TAG, "Device registration failed");
                    }
                });
            }
        }
    }

    private static class GetAuthTokenTask extends AsyncTask<Void, Void, String> {
        Auth mAuth;

        GetAuthTokenTask(Auth auth) {
            mAuth = auth;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return fetchToken();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String token) {
            mAuth.setGoogleAuthToken(token);

            if (token == null) {
                mAuth.callbacks(Step.AUTHENTICATION_FAILED);
            }
        }

        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mAuth.mActivity, mAuth.mEmail, SCOPE);
            } catch (UserRecoverableAuthException e) {
                mAuth.mActivity.startActivityForResult(e.getIntent(), Config.REQUEST_CODE_AUTH_RESOLUTION);
            } catch (GoogleAuthException | IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public Auth(Team t) {
        team = t;

        mCallbacks = new HashSet<>();

        load();
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public void save() {
        //XXX todo update user object

        team.preferences.edit()
                .putString(Config.PREFERENCE_USER, mUser)
                .putString(Config.PREFERENCE_AUTH_TOKEN, mAuthToken)
                .putString(Config.PREFERENCE_GCM_REGISTRATION_ID, mGcmRegistrationId)
                .putString(Config.PREFERENCE_SOCIAL_MODE, mSocialMode)
                .apply();
    }

    public void load() {
        mUser = team.preferences.getString(Config.PREFERENCE_USER, null);
        mAuthToken = team.preferences.getString(Config.PREFERENCE_AUTH_TOKEN, null);
        mGcmRegistrationId = team.preferences.getString(Config.PREFERENCE_GCM_REGISTRATION_ID, null);
        mSocialMode = team.preferences.getString(Config.PREFERENCE_SOCIAL_MODE, Config.SOCIAL_MODE_ON);

        Log.d(Config.LOG_TAG, "user = " + mUser);

        registerDevice();
    }

    public DynamicRealmObject me() {
        if(mUser == null)
            return null;

        return team.realm.where("Thing").equalTo("id", mUser).findFirst();
    }

    public String getUser() {
        return mUser;
    }

    public boolean isAuthenticated() {
        return mUser != null && mAuthToken != null;
    }

    public String getAuthParam() {
        if(!isAuthenticated())
            return null;

        return mAuthToken;
    }

    public void showMain() {
        team.view.showStartView(mActivity);
    }

    public void reauth() {
        unregisterDevice();

        if(mGoogleAuthToken != null && mActivity != null) {
            GoogleAuthUtil.invalidateToken(mActivity, mGoogleAuthToken);
        }

        boolean isLogout = (mUser != null);


        mUser = null;
        mEmail = null;
        mAuthToken = null;
        mGoogleAuthToken = null;
        mSocialMode = Config.SOCIAL_MODE_ON;
        save();

        if(isLogout)
            team.view.showStartView(mActivity);
    }

    public void signin() {
        if(isAuthenticated()) {
            showMain();
        }
        else {
            if(mEmail == null) {
                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[] {"com.google"},
                        false, null, null, null, null);

                mActivity.startActivityForResult(intent, Config.REQUEST_CODE_ACCOUNT_PICKER);
            }
            else if(mGoogleAuthToken == null) {
                fetchAuthToken();
            }
            else if(mUser == null) {
                RequestParams params = new RequestParams();
                params.put(Config.PARAM_EMAIL, mEmail);
                params.put(Config.PARAM_AUTH, mGoogleAuthToken);

                team.api.get(Config.PATH_EARTH + "/" + Config.PATH_ME, params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        JsonObject o = Json.from(response, JsonObject.class);

                        if(o.has("auth"))
                            setAuthToken(o.get("auth").getAsString());

                        if(o.has("social_mode"))
                            setSocialMode(o.get("social_mode").getAsString());

                        setUser(team.things.put(response));
                        callbacks(Step.COMPLETE);
                    }

                    @Override
                    public void fail(String response) {
                        callbacks(Step.AUTHENTICATION_FAILED);
                    }
                });
            }
        }
    }

    public void logout(@NonNull final Activity activity) {
        setActivity(activity);
        reauth();
        activity.finish();
    }

    public void callback(Callback callback) {
        if(mGoogleAuthToken != null) {
            callback.onStep(Step.AUTHENTICATED);
        }

        if(mUser != null) {
            callback.onStep(Step.COMPLETE);
        }

        mCallbacks.add(callback);
    }

    private void callbacks(Step step) {
        for(Callback callback : mCallbacks) {
            callback.onStep(step);
        }
    }

    public void setGcmRegistrationId(String regId) {
        mGcmRegistrationId = regId;
        save();
    }

    private void setEmail(String email) {
        mEmail = email;
    }

    private void setUser(DynamicRealmObject user) {
        if(user == null)
            return;

        mUser = user.getString(Thing.ID);

        registerDevice();
        save();
        signin();
    }

    private void setGoogleAuthToken(String auth) {
        mGoogleAuthToken = auth;
        callbacks(Step.AUTHENTICATED);
        signin();
    }

    private void setAuthToken(String auth) {
        mAuthToken = auth;
        save();
    }

    public void updateSocialMode(String socialMode) {
        setSocialMode(socialMode);
        registerDevice();
    }

    private void setSocialMode(String socialMode) {
        mSocialMode = socialMode;
        save();
    }

    public String getSocialMode() {
        return mSocialMode;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.REQUEST_CODE_ACCOUNT_PICKER:
                if(resultCode == Activity.RESULT_OK && data != null) {
                    setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    fetchAuthToken();
                } else {
                    callbacks(Step.AUTHENTICATION_CANCELED);
                }
                break;
            case Config.REQUEST_CODE_AUTH_RESOLUTION:
                if(resultCode == Activity.RESULT_OK && data != null) {
                    setGoogleAuthToken(data.getStringExtra("authtoken"));
                } else {
                    callbacks(Step.AUTHENTICATION_FAILED);
                }

                break;
        }
    }

    /**
     * Check if a permission is granted.  If it isn't, it will be requested.
     *
     * @return if the permission is already granted
     */
    public boolean checkPermission(@NonNull final Activity activity, @NonNull final String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, Config.REQUEST_CODE_REQUEST_PERMISSION);

            return false;
        }

        return true;
    }

    private void fetchAuthToken() {
        if(mFetchTask != null && mFetchTask.getStatus() != AsyncTask.Status.FINISHED)
            return;

        mFetchTask = new GetAuthTokenTask(this);
        mFetchTask.execute();
    }

    private void registerDevice() {
        if(mUser != null) {
            if(mRegisterDeviceRunnable == null) {
                final Auth auth = this;

                mRegisterDeviceRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GcmRegistrationAsyncTask(auth).execute();
                    }
                };
            }

            if(mHandler == null) {
                mHandler = new Handler(Looper.getMainLooper());
            }

            mHandler.removeCallbacks(mRegisterDeviceRunnable);
            mHandler.postDelayed(mRegisterDeviceRunnable, 1500);
        }
    }

    private void unregisterDevice() {
        if(mUser != null && mGcmRegistrationId != null) {
            RequestParams params = new RequestParams();
            params.put(Config.PARAM_DEVICE_ID, mGcmRegistrationId);

            team.api.post(Config.PATH_EARTH + "/" + Config.PATH_ME_UNREGISTER_DEVICE, params, new Api.Callback() {
                @Override
                public void success(String response) {
                    Log.w(Config.LOG_TAG, "Device successfully unregistered");
                }

                @Override
                public void fail(String response) {
                    Log.w(Config.LOG_TAG, "Device unregistration failed");
                }
            });
        }
    }
}