package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.MainActivity;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 10/19/14.
 */
public class ExploreSlide extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.explore, container, false);

        View.OnClickListener oclk = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.push(ViewActivity.Transition.SEXY_PROFILE, ViewActivity.Transition.IN_THE_VOID, ((MainActivity) team.view).mPersonView);
            }
        };

        View.OnClickListener oclk_map = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.search("");
            }
        };

        view.findViewById(R.id.profilelink).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink2).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink3).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink4).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink5).setOnClickListener(oclk);
        view.findViewById(R.id.profilelink6).setOnClickListener(oclk);
        view.findViewById(R.id.maplink).setOnClickListener(oclk_map);

        return view;
    }
}
