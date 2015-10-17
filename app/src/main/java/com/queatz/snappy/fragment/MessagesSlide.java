package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.ContactAdapter;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Contact;
import com.queatz.snappy.things.Message;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmResults;

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

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                team.action.openMessages(getActivity(), ((Contact) mList.getAdapter().getItem(position)).getContact());
            }
        });

        refresh();
        update();

        return view;
    }

    private void update() {
        if(team.auth.getUser() != null) {
            if(mList.getAdapter() == null) {
                RealmResults<Contact> contacts = team.realm.where(Contact.class)
                        .equalTo("person.id", team.auth.getUser())
                        .findAllSorted("updated", false);

                mList.setAdapter(new ContactAdapter(getActivity(), contacts));
            }

            ((ViewGroup) emptyView).getChildAt(0).setVisibility(mList.getAdapter().getCount() < 1 ? View.VISIBLE : View.GONE);
        }
    }

    private void refresh() {
        if(getActivity() == null)
            return;

        team.api.get(Config.PATH_MESSAGES, new Api.Callback() {
            @Override
            public void success(String response) {
                mRefresh.setRefreshing(false);

                try {
                    JSONObject o = new JSONObject(response);

                    if(o.has("messages"))
                        team.things.putAll(Message.class, o.getJSONArray("messages"));

                    if(o.has("contacts"))
                        team.things.putAll(Contact.class, o.getJSONArray("contacts"));

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                update();
            }

            @Override
            public void fail(String response) {
                mRefresh.setRefreshing(false);
            }
        });
    }
}
