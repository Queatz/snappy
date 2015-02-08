package com.queatz.snappy;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.fragment.HostParty;
import com.queatz.snappy.fragment.Main;
import com.queatz.snappy.fragment.NewUpto;
import com.queatz.snappy.fragment.Person;
import com.queatz.snappy.fragment.PersonList;
import com.queatz.snappy.fragment.Signin;
import com.queatz.snappy.fragment.Upto;
import com.queatz.snappy.team.Team;

public class MainActivity extends ViewActivity {
    public Team team;

    public Fragment mSigninView;
    public Fragment mMainView;
    public Fragment mPersonView;
    public Fragment mUptoView;
    public Fragment mNewUpto;
    public Fragment mHostParty;
    public Fragment mPersonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;
        team.view = this;

        mSigninView = new Signin();
        mMainView = new Main();
        mPersonView = new Person();
        mUptoView = new Upto();
        mNewUpto= new NewUpto();
        mHostParty = new HostParty();
        mPersonList = new PersonList();

        showStartView();
        onNewIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        if(intent != null) {
            Log.d(Config.TAG, "new action! " + intent.getAction() + " | " + intent.getType());

            if(intent.getAction().equals(Intent.ACTION_SEND)) {
                ((NewUpto) team.view.mNewUpto).setintent(intent);
                team.view.push(Transition.EXAMINE, Transition.INSTANT, team.view.mNewUpto);
            }
        }
    }

    @Override
    protected void onDestroy() {
        team.view = null;

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        team.auth.onActivityResult(requestCode, resultCode, data);
    }

    // Functions

    public void showStartView() {
        replace(team.auth.isAuthenticated() ? mMainView : mSigninView);
        setDeparture(ViewActivity.Transition.GRAND_REVEAL);
    }
}
