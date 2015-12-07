package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.TextView;

/**
 * Created by jacob on 12/6/15.
 */
public class SettingsSlide extends Fragment {
    com.queatz.snappy.things.Person mPerson;
    Team team;

    public void setPerson(com.queatz.snappy.things.Person person) {
        mPerson = person;
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

        socialMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSocialMode();
            }
        });

        updateSettings(view);

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
        socialModeState.setTextColor(getResources().getColor(Config.SOCIAL_MODE_OFF.equals(team.auth.getSocialMode()) ? R.color.red : R.color.green));
    }
}
