package com.queatz.snappy;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.queatz.snappy.activity.HostParty;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.Welcome;
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
    public void onNewIntent(Intent intent) {
        if(intent != null) {
            Log.d(Config.LOG_TAG, "new action! " + intent.getAction() + " | " + intent.getType());

            if(intent.getAction().equals(Intent.ACTION_SEND)) {
                //((NewUpto) team.view.mNewUpto).setintent(intent);
                //team.view.push(Transition.EXAMINE, Transition.INSTANT, team.view.mNewUpto);
            }
        }
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
