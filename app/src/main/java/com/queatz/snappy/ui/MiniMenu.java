package com.queatz.snappy.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.HostParty;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Bounty;
import com.queatz.snappy.things.Person;

import java.util.Date;

import io.realm.RealmQuery;

/**
 * Created by jacob on 1/3/15.
 */

public class MiniMenu extends FrameLayout {
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
        updateBountiesText();

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
                Person person = team.things.get(Person.class, team.auth.getUser());

                team.action.openProfile((Activity) getContext(), person);

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

        findViewById(R.id.action_bounties).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openBounties((Activity) getContext());
                show(false);
            }
        });
    }

    private void updateBountiesText() {
        final Team team = ((MainApplication) getContext().getApplicationContext()).team;

        RealmQuery<Bounty> query = team.realm.where(Bounty.class).greaterThan("posted", new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7))
                .notEqualTo("status", Config.BOUNTY_STATUS_FINISHED)
                .beginGroup()
                    .notEqualTo("status", Config.BOUNTY_STATUS_CLAIMED)
                    .or()
                    .equalTo("poster.id", team.auth.getUser())
                    .or()
                    .equalTo("people.id", team.auth.getUser())
                .endGroup();

        long bounties = query.count();

        findViewById(R.id.action_bounties).setVisibility(View.VISIBLE);

        if(0 == bounties) {
            if(Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled())) {
                ((TextView) findViewById(R.id.action_bounties)).setText(getResources().getString(R.string.post_a_bounty));
            }
            else {
                findViewById(R.id.action_bounties).setVisibility(View.GONE);
            }
        }
        else {
            ((TextView) findViewById(R.id.action_bounties)).setText(getResources().getQuantityString(R.plurals.bounties, (int) bounties, bounties));
        }
    }

    private void updateSocialModeText(String socialMode) {
        TextView socialModeTextView = (TextView) findViewById(R.id.social_mode);

        socialModeTextView.setText(socialMode.substring(0, 1).toUpperCase() + socialMode.substring(1));
        socialModeTextView.setTextColor(getResources().getColor(Config.SOCIAL_MODE_OFF.equals(socialMode) ? R.color.whiteout : R.color.lightblue));
    }

    public void show() {
        final Team team = ((MainApplication) getContext().getApplicationContext()).team;
        findViewById(R.id.action_host).setVisibility(Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled()) ? View.VISIBLE : View.GONE);
        show(getVisibility() == View.GONE);
    }

    public void show(boolean show) {
        setPivotX(getWidth());
        setPivotY(0);

        if(show) {
            updateBountiesText();
            RevealAnimation.expand(this);
        }
        else {
            RevealAnimation.collapse(this);
        }
    }
}
