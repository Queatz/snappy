package com.queatz.snappy.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.TeamFragment;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.SendFeedbackAction;
import com.queatz.snappy.team.actions.ShowAboutAction;
import com.queatz.snappy.team.actions.ShowPrivacyPolicyAction;
import com.queatz.snappy.team.actions.ShowTermsOfServiceAction;
import com.queatz.snappy.team.actions.SignoutAction;
import com.queatz.snappy.ui.TextView;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 12/6/15.
 */
public class SettingsSlide extends TeamFragment {
    DynamicRealmObject mPerson;
    Team team;

    public void setPerson(DynamicRealmObject person) {
        mPerson = person;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPerson == null) {
            return;
        }

        outState.putString(Config.EXTRA_PERSON_ID, mPerson.getString(Thing.ID));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = ((MainApplication) getActivity().getApplication()).team;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);

        View socialMode = view.findViewById(R.id.action_socialmode);
        View halp = view.findViewById(R.id.action_info);
        View privacyPolicy = view.findViewById(R.id.action_privacy_policy);
        View termsOfService = view.findViewById(R.id.action_terms_of_service);

        socialMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSocialMode();
            }
        });

        halp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to(new ShowAboutAction());
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to(new ShowPrivacyPolicyAction());
            }
        });

        termsOfService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to(new ShowTermsOfServiceAction());
            }
        });

        updateSettings(view);

        view.findViewById(R.id.action_send_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to(new SendFeedbackAction());
            }
        });

        view.findViewById(R.id.action_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to(new SignoutAction());
            }
        });

        return view;
    }

    private void toggleSocialMode() {
        team.auth.updateSocialMode(Util.nextSocialMode(team.auth.getSocialMode()));
        updateSettings(getView());
    }

    private void updateSettings(View view) {
        if (view == null) {
            return;
        }

        TextView socialModeState = (TextView) view.findViewById(R.id.social_mode);

        socialModeState.setText(team.auth.getSocialMode());
        socialModeState.setTextColor(getResources().getColor(Config.SOCIAL_MODE_OFF.equals(team.auth.getSocialMode()) ? R.color.gray : R.color.green));
    }
}
