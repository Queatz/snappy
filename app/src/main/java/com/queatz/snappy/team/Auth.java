package com.queatz.snappy.team;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.things.Person;

import java.io.IOException;

/**
 * Created by jacob on 10/19/14.
 */
public class Auth {
    private static final String SCOPE = "oauth2: email profile";

    public Team team;
    private String mAuthToken;
    private String mEmail;
    private String mUser;
    private GetAuthTokenTask mFetchTask;

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
            mAuth.setAuthToken(token);
        }

        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mAuth.team.view, mAuth.mEmail, SCOPE);
            } catch (UserRecoverableAuthException e) {
                mAuth.team.view.startActivityForResult(e.getIntent(), Config.REQUEST_CODE_AUTH_RESOLUTION);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public Auth(Team t) {
        team = t;

        load();
    }

    public void save() {
        team.preferences.edit()
                .putString(Config.PREFERENCE_USER, mUser)
                .putString(Config.PREFERENCE_AUTH_TOKEN, mAuthToken)
                .apply();
    }

    public void load() {
        mUser = team.preferences.getString(Config.PREFERENCE_USER, null);
        mAuthToken = team.preferences.getString(Config.PREFERENCE_AUTH_TOKEN, null);

        Log.d(Config.LOG_TAG, "user = " + mUser);
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
        team.view.pop(new ViewActivity.OnCompleteCallback() {
            @Override
            public void onComplete() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        team.view.push(ViewActivity.Transition.SPACE_GAME, null, team.view.mMainView);
                    }
                }, 0);
            }
        });
    }

    public void reauth() {
        if(mAuthToken != null) {
            GoogleAuthUtil.invalidateToken(team.view, mAuthToken);
        }

        mUser = null;
        mEmail = null;
        mAuthToken = null;
        save();

        team.view.showStartView();
    }

    public void signin() {
        if(isAuthenticated()) {
            showMain();
        }
        else {
            if(mEmail == null) {
                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[] {"com.google"},
                        false, null, null, null, null);

                team.view.startActivityForResult(intent, Config.REQUEST_CODE_ACCOUNT_PICKER);
            }
            else if(mAuthToken == null) {
                fetchAuthToken();
            }
            else if(mUser == null) {
                RequestParams params = new RequestParams();
                params.put(Config.PARAM_EMAIL, mEmail);
                params.put(Config.PARAM_AUTH, mAuthToken);

                team.api.get(Config.PATH_ME, params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        setUser(team.things.put(Person.class, response));
                    }

                    @Override
                    public void fail(String response) {

                    }
                });
            }
        }
    }

    private void setEmail(String email) {
        mEmail = email;
    }

    private void setUser(Person user) {
        if(user == null)
            return;

        mUser = user.getId();

        save();
        signin();
    }

    private void setAuthToken(String auth) {
        mAuthToken = auth;
        save();
        signin();
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
                    setAuthToken(data.getStringExtra("authtoken"));
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
}