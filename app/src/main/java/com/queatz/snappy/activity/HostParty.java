package com.queatz.snappy.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
public class HostParty extends Activity {
    public Team team;
    private String mGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;
        mGroup = null;

        setContentView(R.layout.host_party);

        final ListView partyList = ((ListView) findViewById(R.id.partyList));

        /* New Party */

        final View newParty = View.inflate(this, R.layout.host_party_new, null);

        RealmResults<Party> recentParties = team.realm.where(Party.class)
                .equalTo("host.id", team.auth.getUser())
                .findAll();
        recentParties.sort("date", false);
        partyList.setAdapter(new HostPartyAdapter(this, recentParties));

        View.OnClickListener oclk = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Team team = ((MainApplication) getApplication()).team;

                String name = ((EditText) newParty.findViewById(R.id.name)).getText().toString();
                String date = ((EditText) newParty.findViewById(R.id.date)).getText().toString();
                String location = ((EditText) newParty.findViewById(R.id.location)).getText().toString();
                String details = ((EditText) newParty.findViewById(R.id.details)).getText().toString();

                if(name.isEmpty()) {
                    Toast.makeText(HostParty.this, ((EditText) newParty.findViewById(R.id.name)).getHint().toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(date.isEmpty()) {
                    Toast.makeText(HostParty.this, ((EditText) newParty.findViewById(R.id.date)).getHint().toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(location.isEmpty()) {
                    Toast.makeText(HostParty.this, ((EditText) newParty.findViewById(R.id.location)).getHint().toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

                team.action.hostParty(mGroup, name, date, location, details);
                finish();
            }
        };

        newParty.findViewById(R.id.action_host).setOnClickListener(oclk);

        partyList.addHeaderView(newParty);
        partyList.addFooterView(new View(this));

        partyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setParty((Party) partyList.getAdapter().getItem(position));
            }
        });
    }

    public void setParty(Party party) {
        mGroup = party.getId();

        EditText name;

        name = ((EditText) findViewById(R.id.name));
        name.setText(party.getName());
        name.setEnabled(false);

        name = ((EditText) findViewById(R.id.date));
        name.setText(Util.cuteDate(party.getDate()));

        name = ((EditText) findViewById(R.id.location));
        name.setText(party.getLocation().getName());

        name = ((EditText) findViewById(R.id.details));
        name.setText(party.getDetails());

        ((ListView) findViewById(R.id.partyList)).smoothScrollToPosition(0);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getMenuInflater();
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

