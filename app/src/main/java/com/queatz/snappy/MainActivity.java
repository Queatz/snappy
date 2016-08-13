package com.queatz.snappy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.queatz.snappy.team.Team;

public class MainActivity extends Activity {
    public Team team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;

        team.view.showStartView(this);

        onNewIntent(getIntent());
        finish();
    }

    @Override
    protected void onDestroy() {
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
}
