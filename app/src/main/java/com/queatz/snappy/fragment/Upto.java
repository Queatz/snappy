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

        registerForContextMenu(view.findViewById(R.id.commentclick));
        registerForContextMenu(view.findViewById(R.id.commentclick2));

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();

        int m;

        if(v.getId() == R.id.commentclick || v.getId() == R.id.commentclick2)
            m = R.menu.comment;
        else
            m = R.menu.upto;

        inflater.inflate(m, menu);
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
