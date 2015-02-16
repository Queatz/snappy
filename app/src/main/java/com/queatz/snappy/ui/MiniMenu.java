package com.queatz.snappy.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.team.Team;

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
        findViewById(R.id.action_host).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getContext().getApplicationContext()).team;

                team.view.push(ViewActivity.Transition.EXAMINE, ViewActivity.Transition.INSTANT, team.view.mHostParty);

                show(false);
            }
        });

        findViewById(R.id.action_profile).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getContext().getApplicationContext()).team;

                team.view.push(ViewActivity.Transition.SEXY_PROFILE, ViewActivity.Transition.IN_THE_VOID, team.view.mPersonView);

                show(false);
            }
        });

        findViewById(R.id.action_invite).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                show(false);
            }
        });

        findViewById(R.id.action_socialmode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        findViewById(R.id.action_logout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getContext().getApplicationContext()).team;

                team.auth.reauth();

                show(false);
            }
        });
    }

    public void show() {
        show(getVisibility() == View.GONE);
    }

    public void show(boolean show) {
        setPivotX(getWidth());
        setPivotY(0);

        if(show) {
            setVisibility(View.VISIBLE);
            setScaleY(0);
            animate()
                    .scaleY(1)
                    .setDuration(150)
                    .setInterpolator(new OvershootInterpolator())
                    .setListener(null);
        }
        else {
            animate()
                    .setDuration(150)
                    .scaleY(0)
                    .setInterpolator(new AnticipateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setVisibility(View.GONE);
                        }
                    });
        }
    }
}
