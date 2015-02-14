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
import com.queatz.snappy.Config;
import com.queatz.snappy.activity.ViewActivity;

import java.io.IOException;

/**
 * Created by jacob on 10/19/14.
 */
public class Auth {
    private static final String SCOPE = "oauth2: email profile";

    public Team team;
    private String mAuthToken;
    private String mUser;
    private GetAuthTokenTask mFetchTask;

    private static class GetAuthTokenTask extends AsyncTask<Void, Void, String> {
        Auth mAuth;

        GetAuthTokenTask (Auth auth) {
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
                return GoogleAuthUtil.getToken(mAuth.team.view, mAuth.mUser, SCOPE);
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
    }

    public boolean isAuthenticated() {
        return mUser != null && mAuthToken != null;
    }

    public String getAuthParam() {
        if(!isAuthenticated())
            return null;

        return mUser + ";" + mAuthToken;
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
                }, 1000);
            }
        });
    }

    public void reauth() {
        if(mAuthToken != null) {
            GoogleAuthUtil.invalidateToken(team.view, mAuthToken);
        }

        mUser = null;
        mAuthToken = null;
        save();

        team.view.showStartView();
    }

    public void signin() {
        if(isAuthenticated()) {
            showMain();
        }
        else {
            if(mUser == null) {
                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                        false, null, null, null, null);

                team.view.startActivityForResult(intent, Config.REQUEST_CODE_ACCOUNT_PICKER);
            }
            else if(mAuthToken == null) {
                fetchAuthToken();
            }
        }
    }

    private void setUser(String user) {
        mUser = user;
        save();
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
                    setUser(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    fetchAuthToken();
                }
                break;
            case Config.REQUEST_CODE_AUTH_RESOLUTION:
                if(resultCode == Activity.RESULT_OK && data != null) {
                    setAuthToken(data.getStringExtra("authtoken"));
                }

                break;
        }

        Log.d(Config.TAG, "Auth.onActRes " + requestCode + " " + resultCode + " " + data);

        if(data != null && data.getExtras() != null) for (String key : data.getExtras().keySet()) {
            Log.d(Config.TAG, key + ": " + data.getExtras().get(key));
        }
    }

    private void fetchAuthToken() {
        if(mFetchTask != null && mFetchTask.getStatus() != AsyncTask.Status.FINISHED)
            return;

        mFetchTask = new GetAuthTokenTask(this);
        mFetchTask.execute();
    }
}