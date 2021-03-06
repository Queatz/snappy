package com.queatz.snappy.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.HostParty;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.team.contexts.ActivityContext;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 1/3/15.
 */

public class MiniMenu extends FrameLayout implements Branchable<ActivityContext> {
    public MiniMenu(android.content.Context context) {
        super(context);
        init();
    }

    public MiniMenu(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniMenu(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void to(Branch<ActivityContext> branch) {
        Branch.from((ActivityContext) getContext()).to(branch);
    }

    private void init() {
        setVisibility(View.GONE);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                show(false);
            }
        });

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.minimenu, this, true);

        setupActions();
    }

    private void setupActions() {
        final Team team = ((MainApplication) getContext().getApplicationContext()).team;

        updateSocialModeText(team.auth.getSocialMode());
        updateQuestsText();

        findViewById(R.id.action_host).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                team.view.show((android.app.Activity) getContext(), HostParty.class, null);
                show(false);
            }
        });

        findViewById(R.id.action_profile).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DynamicRealmObject person = team.things.get(team.auth.getUser());

                to(new OpenProfileAction(person));

                show(false);
            }
        });

        findViewById(R.id.action_information).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(getContext(), R.layout.information, null);

                new AlertDialog.Builder(getContext())
                        .setView(view)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        });

        findViewById(R.id.action_socialmode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String socialMode = Util.nextSocialMode(team.auth.getSocialMode());

                team.auth.updateSocialMode(socialMode);
                updateSocialModeText(socialMode);
            }
        });
    }

    private void updateQuestsText() {
//        final Team team = ((MainApplication) getContext().getApplicationContext()).team;

        findViewById(R.id.action_quests).setVisibility(View.GONE);

//        if(Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled())) {
//            findViewById(R.id.action_quests).setVisibility(View.VISIBLE);
//        }
//        else {
//            findViewById(R.id.action_quests).setVisibility(View.GONE);
//        }
    }

    private void updateSocialModeText(String socialMode) {
        TextView socialModeTextView = (TextView) findViewById(R.id.social_mode);

        socialModeTextView.setText(socialMode.substring(0, 1).toUpperCase() + socialMode.substring(1));
        socialModeTextView.setTextColor(getResources().getColor(Config.SOCIAL_MODE_OFF.equals(socialMode) ? R.color.whiteout : R.color.lightblue));
    }

    public void show() {
        final Team team = ((MainApplication) getContext().getApplicationContext()).team;
        findViewById(R.id.action_host).setVisibility(View.GONE);
//        findViewById(R.id.action_host).setVisibility(Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled()) ? View.VISIBLE : View.GONE);
        show(getVisibility() == View.GONE);
    }

    public void show(boolean show) {
        setPivotX(getWidth());
        setPivotY(0);

        if(show) {
            updateQuestsText();
            RevealAnimation.expand(this);
        }
        else {
            RevealAnimation.collapse(this);
        }
    }
}
