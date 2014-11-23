package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.ActionBar;

/**
 * Created by jacob on 11/23/14.
 */
public class Upto extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upto_expanded, container, false);

        View.OnClickListener oclk = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.push(ViewActivity.Transition.SEXY_PROFILE, ViewActivity.Transition.INSTANT, team.view.mPersonView);
            }
        };

        View.OnClickListener oclk_map = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.search("");
            }
        };

        View upto = view.findViewById(R.id.uptolink);
        registerForContextMenu(upto);

        view.findViewById(R.id.profilelink).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink2).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink3).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink4).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink5).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink6).setOnClickListener(oclk);
        view.findViewById(R.id.maplink).setOnClickListener(oclk_map);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }
}
