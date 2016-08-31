package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.RecentAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.Json;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 10/26/14.
 */
public class MessagesSlide extends Fragment {
    Team team;
    SwipeRefreshLayout mRefresh;
    View emptyView;
    ListView mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.messages, container, false);
        emptyView = inflater.inflate(R.layout.messages_empty, null);

        mRefresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mRefresh.setColorSchemeResources(R.color.red);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mList = (ListView) view.findViewById(R.id.recentList);
        mList.addHeaderView(emptyView);
        mList.setSelectionAfterHeaderView();

        refresh();
        update();

        return view;
    }

    private void update() {
        if(team.auth.getUser() != null) {
            if(mList.getAdapter() == null) {
                RealmResults<DynamicRealmObject> recents = team.realm.where("Thing")
                        .equalTo(Thing.KIND, "recent")
                        .beginGroup()
                            .equalTo("latest.to.id", team.auth.getUser())
                            .or()
                            .equalTo("latest.from.id", team.auth.getUser())
                        .endGroup()
                        .findAllSorted(Thing.UPDATED, Sort.DESCENDING);

                mList.setAdapter(new RecentAdapter(getActivity(), recents));
            }

            boolean noMessages = mList.getAdapter().getCount() < 2;

            ((ViewGroup) emptyView).getChildAt(0).setVisibility(noMessages ? View.VISIBLE : View.GONE);
        }
    }

    private void refresh() {
        if(getActivity() == null)
            return;

        team.api.get(Config.PATH_EARTH + "/" + Config.PATH_ME + "/" + Config.PATH_MESSAGES, new Api.Callback() {
            @Override
            public void success(String response) {
                mRefresh.setRefreshing(false);

                JsonObject o = Json.from(response, JsonObject.class);

                if(o.has("messages"))
                    team.things.putAll(o.getAsJsonArray("messages"));

                if(o.has("contacts"))
                    team.things.putAll(o.getAsJsonArray("contacts"));

                update();
            }

            @Override
            public void fail(String response) {
                mRefresh.setRefreshing(false);
            }
        });
    }
}
