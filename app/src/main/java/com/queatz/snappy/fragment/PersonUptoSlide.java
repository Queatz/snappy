package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.adapter.PartyAdapter;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonUptoSlide extends Fragment {
    Team team;

    SwipeRefreshLayout mRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = ((MainApplication) getActivity().getApplication()).team;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_upto, container, false);

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

        View.OnClickListener oclk_list = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Team team = ((MainApplication) getActivity().getApplication()).team;

                team.view.push(ViewActivity.Transition.EXAMINE, ViewActivity.Transition.INSTANT, team.view.mPersonList);
            }
        };

        view.findViewById(R.id.list1).setOnClickListener(oclk_list);
        view.findViewById(R.id.list2).setOnClickListener(oclk_list);

//        ((TextView) view.findViewById(R.id.upto_1)).setText(getText(R.string.TEMP_update_1));
//        ((TextView) view.findViewById(R.id.upto_2)).setText(getText(R.string.TEMP_update_2));
//        ((TextView) view.findViewById(R.id.upto_3)).setText(getText(R.string.TEMP_update_3));

        return view;
    }

    public void refresh() {
        if(getActivity() == null)
            return;

        team.api.get(Config.PATH_PEOPLE + "/THE_ID", new Api.Callback() {
            @Override
            public void success(String response) {
                try {
                    JSONArray list = new JSONArray(response);
                    List<JSONObject> l = new ArrayList<>();
                    for (int i = 0; i < list.length(); i++) l.add(list.getJSONObject(i));

                    //mList.setAdapter(new PartyAdapter(getActivity(), l));
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
}
