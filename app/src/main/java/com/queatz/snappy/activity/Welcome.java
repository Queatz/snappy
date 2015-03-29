package com.queatz.snappy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.team.Auth;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 10/19/14.
 */
public class Welcome extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Team team = ((MainApplication) getApplication()).team;

        team.auth.setActivity(this);

        setContentView(R.layout.welcome);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                team.auth.signin();
            }
        });

        team.auth.callback(new Auth.Callback() {
            @Override
            public void onStep(Auth.Step step) {
                if(step == Auth.Step.AUTHENTICATED) {
                    team.view.show(Welcome.this, Buy.class, null);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Team team = ((MainApplication) getApplication()).team;
        team.auth.onActivityResult(requestCode, resultCode, data);
    }
}