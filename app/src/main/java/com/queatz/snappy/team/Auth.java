package com.queatz.snappy.team;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;
import com.queatz.snappy.things.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;

/**
 * Created by jacob on 10/19/14.
 */
public class Auth {
    private static final String SCOPE = "oauth2: email profile";

    public Team team;
    private String mGoogleAuthToken;
    private String mAuthToken;
    private String mEmail;
    private String mUser;
    private String mGcmRegistrationId;
    private GetAuthTokenTask mFetchTask;
    private Activity mActivity;
    private HashSet<Runnable> mCallbacks;

    private static class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
        private GoogleCloudMessaging gcm;
        private Auth auth;

        private static final String SENDER_ID = "1098230558363";

        public GcmRegistrationAsyncTask(Auth auth) {
            this.auth = auth;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(auth.team.context);
                }

                return gcm.register(SENDER_ID);
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

                auth.team.api.post(Config.PATH_ME_REGISTER_DEVICE, params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        Log.w(Config.LOG_TAG, "Device successfully registered");
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
        }

        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mAuth.mActivity, mAuth.mEmail, SCOPE);
            } catch (UserRecoverableAuthException e) {
                mAuth.mActivity.startActivityForResult(e.getIntent(), Config.REQUEST_CODE_AUTH_RESOLUTION);
            } catch (GoogleAuthException e) {
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
        team.preferences.edit()
                .putString(Config.PREFERENCE_USER, mUser)
                .putString(Config.PREFERENCE_AUTH_TOKEN, mAuthToken)
                .putString(Config.PREFERENCE_GCM_REGISTRATION_ID, mGcmRegistrationId)
                .apply();
    }

    public void load() {
        mUser = team.preferences.getString(Config.PREFERENCE_USER, null);
        mAuthToken = team.preferences.getString(Config.PREFERENCE_AUTH_TOKEN, null);
        mGcmRegistrationId = team.preferences.getString(Config.PREFERENCE_GCM_REGISTRATION_ID, null);

        Log.d(Config.LOG_TAG, "user = " + mUser);

        registerDevice();
    }

    public Person me() {
        if(mUser == null)
            return null;

        return team.realm.where(Person.class).equalTo("id", mUser).findFirst();
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
        if(mGoogleAuthToken != null && mActivity != null) {
            GoogleAuthUtil.invalidateToken(mActivity, mGoogleAuthToken);
        }

        boolean isLogout = (mUser != null);

        unregisterDevice();

        mUser = null;
        mEmail = null;
        mAuthToken = null;
        mGoogleAuthToken = null;
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

                team.api.get(Config.PATH_ME, params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        try {
                            JSONObject o = new JSONObject(response);

                            if(o.has("auth"))
                                setAuthToken(o.getString("auth"));

                            setUser(team.things.put(Person.class, response));
                            callbacks();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void fail(String response) {

                    }
                });
            }
        }
    }

    public void callback(Runnable runnable) {
        mCallbacks.add(runnable);
    }

    private void callbacks() {
        for(Runnable runnable : mCallbacks) {
            runnable.run();
        }
    }

    public void setGcmRegistrationId(String regId) {
        mGcmRegistrationId = regId;
        save();
    }

    private void setEmail(String email) {
        mEmail = email;
    }

    private void setUser(Person user) {
        if(user == null)
            return;

        mUser = user.getId();

        registerDevice();
        save();
        signin();
    }

    private void setGoogleAuthToken(String auth) {
        mGoogleAuthToken = auth;
        signin();
    }

    private void setAuthToken(String auth) {
        mAuthToken = auth;
        save();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.REQUEST_CODE_ACCOUNT_PICKER:
                if(resultCode == Activity.RESULT_OK && data != null) {
                    setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    fetchAuthToken();
                }
                break;
            case Config.REQUEST_CODE_AUTH_RESOLUTION:
                if(resultCode == Activity.RESULT_OK && data != null) {
                    setGoogleAuthToken(data.getStringExtra("authtoken"));
                }

                break;
        }

        Log.d(Config.LOG_TAG, "Auth.onActRes " + requestCode + " " + resultCode + " " + data);

        if(data != null && data.getExtras() != null) for (String key : data.getExtras().keySet()) {
            Log.d(Config.LOG_TAG, key + ": " + data.getExtras().get(key));
        }
    }

    private void fetchAuthToken() {
        if(mFetchTask != null && mFetchTask.getStatus() != AsyncTask.Status.FINISHED)
            return;

        mFetchTask = new GetAuthTokenTask(this);
        mFetchTask.execute();
    }

    private void registerDevice() {
        if(mUser != null) {
            new GcmRegistrationAsyncTask(this).execute();
        }
    }

    private void unregisterDevice() {
        if(mUser != null && mGcmRegistrationId != null) {
            RequestParams params = new RequestParams();
            params.put(Config.PARAM_DEVICE_ID, mGcmRegistrationId);

            team.api.post(Config.PATH_ME_UNREGISTER_DEVICE, params, new Api.Callback() {
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