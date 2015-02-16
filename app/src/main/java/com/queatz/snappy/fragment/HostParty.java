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
import android.widget.EditText;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 1/3/15.
 */
public class HostParty extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.host_party, container, false);

        View.OnClickListener oclk = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                String name = ((EditText) view.findViewById(R.id.name)).getText().toString();
                String date = ((EditText) view.findViewById(R.id.date)).getText().toString();
                String location = ((EditText) view.findViewById(R.id.location)).getText().toString();
                String details = ((EditText) view.findViewById(R.id.details)).getText().toString();

                team.action.hostParty(0, name, date, location, details);
                team.view.pop();
            }
        };

        view.findViewById(R.id.action_host).setOnClickListener(oclk);

        View.OnClickListener oclk2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name;

                name = ((EditText) view.findViewById(R.id.name));
                name.setText("Code and Jazz");
                name.setEnabled(false);

                name = ((EditText) view.findViewById(R.id.date));
                name.setText("5pm");

                name = ((EditText) view.findViewById(R.id.location));
                name.setText("Chestnut Mini-Mansion");

                name = ((EditText) view.findViewById(R.id.details));
                name.setText("Chess night");
            }
        };

        view.findViewById(R.id.click_glam).setOnClickListener(oclk2);
        view.findViewById(R.id.click_code).setOnClickListener(oclk2);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

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

