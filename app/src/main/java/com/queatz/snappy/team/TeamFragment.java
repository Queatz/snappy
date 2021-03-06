package com.queatz.snappy.team;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.team.contexts.TeamContext;
import com.queatz.snappy.team.observers.CurrentEnvironment;
import com.queatz.snappy.team.observers.EnvironmentContext;
import com.queatz.snappy.team.observers.EnvironmentObserver;

/**
 * Created by jacob on 4/2/17.
 */

public class TeamFragment extends Fragment implements TeamContext, Branchable<ActivityContext>, EnvironmentContext {

    private EnvironmentObserver environmentObserver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environmentObserver = getTeam().environment.observe(this);
    }

    @Override
    public void onDestroy() {
        getTeam().environment.forget(this);
        super.onDestroy();
    }

    @Override
    public void when(CurrentEnvironment change) {
        environmentObserver.when(change);
    }

    @Override
    public Team getTeam() {
        return ((MainApplication) getActivity().getApplicationContext()).team;
    }

    @Override
    public void to(Branch<ActivityContext> branch) {
        Branch.from((ActivityContext) getActivity()).to(branch);
    }
}
