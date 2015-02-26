package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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

import io.realm.RealmResults;

/**
 * Created by jacob on 10/19/14.
 */
public class PartiesSlide extends Fragment {
    Team team;

    SwipeRefreshLayout mRefresh;
    ListView mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parties, container, false);

        mList = (ListView) view.findViewById(R.id.list);
        mList.addHeaderView(new View(getActivity()));
        mList.addFooterView(new View(getActivity()));

        mRefresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mRefresh.setColorSchemeResources(R.color.red);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mRefresh.setRefreshing(true);
        update();
        refresh();

        return view;
    }

    public void update() {
        if(getActivity() == null)
            return;

        RealmResults<Party> list = team.realm().where(Party.class)
                .greaterThan("date", new Date(new Date().getTime() - 1000 * 60 * 60))
                .findAllSorted("date", true);
        mList.setAdapter(new PartyAdapter(getActivity(), list));
    }

    public void refresh() {
        if(getActivity() == null)
            return;

        Location location = team.location.get();

        if(location == null)
            return;

        RequestParams params = new RequestParams();

        params.put("latitude", location.getLatitude());
        params.put("longitude", location.getLongitude());

        team.api.get(Config.PATH_PARTIES + "?" + params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.putAll(Party.class, response);
                update();

                mRefresh.setRefreshing(false);
            }

            @Override
            public void fail(String response) {
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
