package com.queatz.snappy;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.queatz.snappy.fragment.Person;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.fragment.Main;
import com.queatz.snappy.fragment.Signin;

public class MainActivity extends ViewActivity {
    public Team team;

    public Fragment mSigninView;
    public Fragment mMainView;
    public Fragment mPersonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;
        team.view = this;

        mSigninView = new Signin();
        mMainView = new Main();
        mPersonView = new Person();

        showStartView();
    }

    @Override
    protected void onDestroy() {
        team = ((MainApplication) getApplication()).team;
        team.view = null;

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        team.auth.toBundle(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        team.auth.onActivityResult(requestCode, resultCode, data);
    }

    // Functions

    public void showStartView() {
        show(team.auth.isSignedIn() ? mMainView : mSigninView);
    }
}
