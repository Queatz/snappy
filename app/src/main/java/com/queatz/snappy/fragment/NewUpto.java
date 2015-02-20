package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainActivity;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 11/23/14.
 */
public class NewUpto extends Fragment {
    private Intent mIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upto_new, container, false);

        view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    public void setintent(Intent intent) {
        mIntent = intent;
    }

    public void done() {
        if(mIntent == null) {
            Log.e(Config.LOG_TAG, "No intent");
            return;
        }

        Team team = ((MainActivity) getActivity()).team;
        team.view.pop();
        //team.view.push(ViewActivity.Transition.SEXY_PROFILE, ViewActivity.Transition.IN_THE_VOID, team.view.mPersonView);

        if(!team.action.uploadUpto((Uri) mIntent.getParcelableExtra(Intent.EXTRA_STREAM), "example location")) {
            Toast.makeText(team.context, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }
}
