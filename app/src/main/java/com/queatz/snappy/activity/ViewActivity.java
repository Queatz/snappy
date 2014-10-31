package com.queatz.snappy.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;

import com.queatz.snappy.R;
import com.queatz.snappy.transition.GrandReveal;
import com.queatz.snappy.transition.InTheVoid;
import com.queatz.snappy.transition.Instant;
import com.queatz.snappy.transition.SexyProfile;
import com.queatz.snappy.transition.SpaceGame;
import com.queatz.snappy.transition.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by jacob on 10/19/14.
 */
public class ViewActivity extends Activity {
    public static interface OnCompleteCallback {
        public void onComplete();
    }

    public enum Transition {
        GRAND_REVEAL,
        IN_THE_VOID,
        SEXY_PROFILE,
        SPACE_GAME,
        INSTANT
    }

    private class BelowAbove {
        public Transition below;
        public Transition above;

        public BelowAbove(Transition b, Transition a) {
            below = b;
            above = a;
        }
    }

    private Stack<BelowAbove> belowAboves;
    private Stack<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragments = new Stack<Fragment>();
        belowAboves = new Stack<BelowAbove>();

        setContentView(R.layout.activity);
    }

    public void push(Transition transition, Transition transition2, Fragment fragment) {
        push(transition, transition2, fragment, null);
    }

    public void push(Transition transition, Transition transition2, Fragment fragment, final OnCompleteCallback onCompleteCallback) {
        Fragment beneath = null;

        if(fragments.size() > 0)
            beneath = fragments.lastElement();

        if(fragment != null && !fragments.contains(fragment)) {
            fragments.push(fragment);

            getFragmentManager().beginTransaction()
                    .add(R.id.activity, fragment)
                    .commit();
        }

        belowAboves.push(new BelowAbove(transition2, transition));

        if(fragment != null) {
            getTransition(transition).fragment(fragment).in();
        }

        if(beneath != null) {
            getTransition(transition2).fragment(beneath).out();
        }
    }

    public void replace(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        while(fragments.size() > 0) {
            transaction.remove(fragments.pop());
        }

        belowAboves.clear();

        if(fragment != null) {
            transaction.add(R.id.activity, fragment);
        }

        transaction.commit();

        fragments.push(fragment);
    }

    public void setDeparture(Transition transition) {
        belowAboves.push(new BelowAbove(transition, transition));
    }

    public void pop() {
        pop(null);
    }

    public void pop(final OnCompleteCallback onCompleteCallback) {
        if(fragments.size() < 1)
            return;

        BelowAbove belowAbove;

        if(belowAboves.size() > 0) {
            belowAbove = belowAboves.pop();
        }
        else {
            belowAbove = new BelowAbove(Transition.IN_THE_VOID, Transition.IN_THE_VOID);
        }

        getTransition(belowAbove.above).fragment(fragments.pop()).onComplete(new com.queatz.snappy.transition.Transition.OnCompleteCallback() {
            @Override
            public void onComplete(Fragment fragment) {
                if(onCompleteCallback != null)
                    onCompleteCallback.onComplete();

                if(fragment != null) {
                    getFragmentManager().beginTransaction()
                            .remove(fragment)
                            .commit();
                }
            }
        }).out();

        if(fragments.size() > 0) {
            getTransition(belowAbove.below).fragment(fragments.lastElement()).in();
        }
    }

    private com.queatz.snappy.transition.Transition getTransition(Transition transition) {
        if(transition == null) {
            return new Instant();
        }

        switch (transition) {
            case SEXY_PROFILE:
                return new SexyProfile();
            case GRAND_REVEAL:
                return new GrandReveal();
            case SPACE_GAME:
                return new SpaceGame();
            case IN_THE_VOID:
                return new InTheVoid();
            case INSTANT:
            default:
                return new Instant();
        }
    }

    public void front(Fragment fragment) {
        if(fragment.getView() != null)
            fragment.getView().bringToFront();
    }

    public int getDepth() {
        return fragments.size();


    }

    @Override
    public void onBackPressed() {
        if(fragments.size() < 2) {
            super.onBackPressed();
            return;
        }

        pop();
    }
}
