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
import android.widget.ListView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.HostPartyAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Party;

import io.realm.RealmResults;

/**
 * Created by jacob on 1/3/15.
 */
public class HostParty extends Fragment {
    public Team team;
    private String mGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;
        mGroup = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.host_party, container, false);

        final ListView partyList = ((ListView) view.findViewById(R.id.partyList));

        /* New Party */

        final View newParty = View.inflate(getActivity(), R.layout.host_party_new, null);

        RealmResults<Party> recentParties = team.realm.where(Party.class).equalTo("host.id", team.auth.getUser()).findAll();
        recentParties.sort("date", false);

        /* TODO Only my parties! */ partyList.setAdapter(new HostPartyAdapter(getActivity(), recentParties));

        View.OnClickListener oclk = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                String name = ((EditText) newParty.findViewById(R.id.name)).getText().toString();
                String date = ((EditText) newParty.findViewById(R.id.date)).getText().toString();
                String location = ((EditText) newParty.findViewById(R.id.location)).getText().toString();
                String details = ((EditText) newParty.findViewById(R.id.details)).getText().toString();

                team.action.hostParty(mGroup, name, date, location, details);
                team.view.pop();
            }
        };

        newParty.findViewById(R.id.action_host).setOnClickListener(oclk);

        partyList.addHeaderView(newParty);
        partyList.addFooterView(new View(getActivity()));

        partyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setParty((Party) partyList.getAdapter().getItem(position));
            }
        });

        /* Host Again */

        return view;
    }

    public void setParty(Party party) {
        mGroup = party.getId();

        if(getView() == null)
            return;

        View view = getView();
        EditText name;

        name = ((EditText) view.findViewById(R.id.name));
        name.setText(party.getName());
        name.setEnabled(false);

        name = ((EditText) view.findViewById(R.id.date));
        name.setText(Util.cuteDate(getActivity(), party.getDate()));

        name = ((EditText) view.findViewById(R.id.location));
        name.setText(party.getLocation().getName());

        name = ((EditText) view.findViewById(R.id.details));
        name.setText(party.getDetails());

        ((ListView) view.findViewById(R.id.partyList)).smoothScrollToPosition(0);
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

