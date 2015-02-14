package com.queatz.snappy.fragment;

import android.app.Fragment;
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

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.PartyAdapter;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 10/19/14.
 */
public class PartiesSlide extends Fragment {
    SwipeRefreshLayout mRefresh;
    ListView mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parties, container, false);

        mList = (ListView) view.findViewById(R.id.list);
        mRefresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);

        mRefresh.setColorSchemeResources(R.color.red);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRefresh.setRefreshing(true);
        refresh();

        return view;
    }

    public void refresh() {
        if(getActivity() == null)
            return;

        Team team = ((MainApplication) getActivity().getApplication()).team;

        team.api.get(Config.PATH_PARTIES, new Api.Callback() {
            @Override
            public void success(String response) {
                try {
                    JSONArray list = new JSONArray(response);
                    List<JSONObject> l = new ArrayList<>();
                        for (int i = 0; i < list.length(); i++) l.add(list.getJSONObject(i));

                    mList.setAdapter(new PartyAdapter(getActivity(), l));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

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
