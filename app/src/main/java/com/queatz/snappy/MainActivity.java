package com.queatz.snappy;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import com.queatz.snappy.fragment.Person;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.fragment.Main;
import com.queatz.snappy.fragment.Signin;
import com.queatz.snappy.ui.ActionBar;
import com.queatz.snappy.ui.FloatingSearch;

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        team.auth.onActivityResult(requestCode, resultCode, data);
    }

    // Functions

    public void search(String s) {
        while(getDepth() > 1) {
            pop();
        }

        ActionBar actionBar = (ActionBar) findViewById(R.id.actionBar);

        actionBar.setPage(1);

        FloatingSearch search = (FloatingSearch) findViewById(R.id.search);

        if(search != null) {
            search.setText(s);
            search.setSelection(s.length());
            search.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
        }
    }

    public void showStartView() {
        replace(team.auth.isAuthenticated() ? mMainView : mSigninView);
        setDeparture(ViewActivity.Transition.GRAND_REVEAL);
    }
}
