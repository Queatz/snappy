package com.queatz.snappy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.BountyAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Bounty;
import com.queatz.snappy.ui.TimeSlider;

import java.util.Date;

import io.realm.RealmResults;

/**
 * Created by jacob on 9/5/15.
 *
 * @deprecated See {@code PersonUptoSlide}
 */
public class Bounties extends Activity {
    Team team;
    Object mContextObject;
    EditText bountyDetails;
    SwipeRefreshLayout mRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;

        setContentView(R.layout.bounties);

        final ListView bountyList = (ListView) findViewById(R.id.bounties);

        if(Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled())) {
            View postBounty = View.inflate(this, R.layout.bounties_new, null);

            bountyList.addHeaderView(postBounty);

            bountyDetails = (EditText) postBounty.findViewById(R.id.details);
            final TimeSlider priceSlider = (TimeSlider) postBounty.findViewById(R.id.price);
            final Button postBountyButton = (Button) postBounty.findViewById(R.id.postBounty);

            priceSlider.setPercent(0);

            priceSlider.setTextCallback(new TimeSlider.TextCallback() {
                @Override
                public String getText(float percent) {
                    int price = getPrice(percent);

                    if (price == 0) {
                        return getString(R.string.free);
                    }

                    return "$" + Integer.toString(price);
                }
            });

            postBountyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.postBounty(bountyDetails.getText().toString(), getPrice(priceSlider.getPercent()));
                    priceSlider.setPercent(0);
                    bountyDetails.setText("");
                }
            });
        }

        final RealmResults<Bounty> bounties = team.realm.where(Bounty.class)
                .greaterThan("posted", new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7))
                .notEqualTo("status", Config.BOUNTY_STATUS_FINISHED)
                .beginGroup()
                    .equalTo("status", Config.BOUNTY_STATUS_OPEN)
                    .or()
                    .equalTo("poster.id", team.auth.getUser())
                    .or()
                    .equalTo("people.id", team.auth.getUser())
                .endGroup()
                .findAllSorted("price", true);

        final BountyAdapter bountyAdapter = new BountyAdapter(this, bounties);
        bountyList.setAdapter(bountyAdapter);

        bountyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bounty bounty = (Bounty) bountyList.getItemAtPosition(position);

                team.action.claimBounty(Bounties.this, bounty);
            }
        });


        mRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mRefresh.setColorSchemeResources(R.color.orange);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void refresh() {
        team.here.update(this, mRefresh, null);
    }

    private int getPrice(float percent) {
        return ((int) (percent * 19) * 10 + 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        team.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        mContextObject = view.getTag();
        team.menu.make(mContextObject, menu, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return team.menu.choose(this, mContextObject, item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
    }
}
