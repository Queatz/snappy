package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

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

                team.view.push(ViewActivity.Transition.SEXY_PROFILE, ViewActivity.Transition.IN_THE_VOID, team.view.mPersonView);
            }
        };

        View.OnClickListener oclk2 = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.push(ViewActivity.Transition.EXAMINE, ViewActivity.Transition.INSTANT, team.view.mUptoView);
            }
        };

        View.OnClickListener oclk_map = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.search("");
            }
        };


        View.OnClickListener oclk_party = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.push(ViewActivity.Transition.EXAMINE, ViewActivity.Transition.INSTANT, team.view.mUptoView);
            }
        };

        view.findViewById(R.id.person1).setOnClickListener(oclk);
        view.findViewById(R.id.card1).setOnClickListener(oclk_party);
        view.findViewById(R.id.card2).setOnClickListener(oclk_party);
        view.findViewById(R.id.card3).setOnClickListener(oclk_party);
        view.findViewById(R.id.card4).setOnClickListener(oclk_party);

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
