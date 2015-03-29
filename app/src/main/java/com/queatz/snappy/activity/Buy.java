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
 * Created by jacob on 3/23/15.
 */
public class Buy extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Team team = ((MainApplication) getApplication()).team;

        team.auth.setActivity(this);

        setContentView(R.layout.buy);

        findViewById(R.id.buy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                team.auth.signin();
            }
        });

        team.auth.callback(new Auth.Callback() {
            @Override
            public void onStep(Auth.Step step) {
                if(step == Auth.Step.PAID)
                    finish();
            }
        });

        team.buy.pull(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Team team = ((MainApplication) getApplication()).team;
        team.auth.onActivityResult(requestCode, resultCode, data);
        team.buy.onActivityResult(requestCode, resultCode, data);
    }
}