package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 10/26/14.
 */
public class MessagesSlide extends Fragment {
    Team team;
    SwipeRefreshLayout mRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.messages, container, false);

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

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.view.push(ViewActivity.Transition.SEXY_PROFILE, ViewActivity.Transition.IN_THE_VOID, team.view.mPersonView);
            }
        };

        view.findViewById(R.id.m1).setOnClickListener(click);
        view.findViewById(R.id.m2).setOnClickListener(click);
        view.findViewById(R.id.m3).setOnClickListener(click);
        view.findViewById(R.id.m4).setOnClickListener(click);

        return view;
    }

    private void refresh() {
        if(getActivity() == null)
            return;

        team.api.get(Config.PATH_MESSAGES, new Api.Callback() {
            @Override
            public void success(String response) {
                try {
                    JSONArray list = new JSONArray(response);
                    List<JSONObject> l = new ArrayList<>();
                    for (int i = 0; i < list.length(); i++) l.add(list.getJSONObject(i));

                    //mList.setAdapter(new MessagesAdapter(getActivity(), l));
                } catch (JSONException e) {
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
}
