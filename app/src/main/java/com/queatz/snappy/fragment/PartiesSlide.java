package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PartyAdapter;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Party;

import java.util.Date;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by jacob on 10/19/14.
 */
public class PartiesSlide extends Fragment implements com.queatz.snappy.team.Location.LocationAvailabilityCallback, RealmChangeListener {
    Team team;

    SwipeRefreshLayout mRefresh;
    ListView mList;
    View emptyView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parties, container, false);
        emptyView = View.inflate(getActivity(), R.layout.parties_empty, null);

        mList = (ListView) view.findViewById(R.id.list);
        mList.addHeaderView(emptyView);
        mList.addFooterView(new View(getActivity()));

        mRefresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mRefresh.setColorSchemeResources(R.color.red);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        update();
        refresh();

        team.location.addLocationAvailabilityCallback(this);
        team.realm.addChangeListener(this);

        return view;
    }

    @Override
    public void onChange() {
        if(getView() != null) {
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateNullState();
                }
            }, 50);
        }
    }

    @Override
    public void onLocationAvailabilityChanged(boolean enabled) {
        if(!enabled) {
            emptyView.findViewById(R.id.enableLocation).setVisibility(View.VISIBLE);
            emptyView.findViewById(R.id.enableLocation).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.location.locate(getActivity());
                }
            });
        }
        else {
            emptyView.findViewById(R.id.enableLocation).setVisibility(View.GONE);
        }
    }

    public void update() {
        if(getActivity() == null)
            return;

        if(mList.getAdapter() == null) {
            String me = team.auth.getUser();

            RealmResults<Party> discoveryList = team.realm.where(Party.class)
                        .greaterThan("date", new Date(new Date().getTime() - 1000 * 60 * 60))
                    .beginGroup()
                        .equalTo("full", false).or()
                        .equalTo("people.person.id", me).or()
                        .equalTo("host.id", me)
                    .endGroup()
                    .findAllSorted("date", true);

            mList.setAdapter(new PartyAdapter(getActivity(), discoveryList));
        }

        Log.w(Config.LOG_TAG, "parties count = " + mList.getAdapter().getCount());

        updateNullState();
    }

    private void updateNullState() {
        emptyView.findViewById(R.id.noParties).setVisibility(mList.getAdapter().isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void refresh() {
        if(getActivity() == null)
            return;

        team.location.get(getActivity(), new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
            @Override
            public void onLocationFound(Location location) {
                RequestParams params = new RequestParams();

                params.put("latitude", location.getLatitude());
                params.put("longitude", location.getLongitude());

                team.api.get(Config.PATH_PARTIES + "?" + params, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        mRefresh.setRefreshing(false);
                        team.things.putAll(Party.class, response);
                        update();
                    }

                    @Override
                    public void fail(String response) {
                        mRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onLocationUnavailable() {
                mRefresh.setRefreshing(false);
            }
        });
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
