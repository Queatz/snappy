package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.adapter.PersonUptoAdapter;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Update;
import com.queatz.snappy.ui.TextView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonUptoSlide extends Fragment {
    Team team;
    com.queatz.snappy.things.Person mPerson;

    public void setPerson(com.queatz.snappy.things.Person person) {
        mPerson = person;
    }

    SwipeRefreshLayout mRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = ((MainApplication) getActivity().getApplication()).team;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_upto, container, false);

        final ListView updateList = ((ListView) view.findViewById(R.id.updateList));

/* New Party */

        final View personAbout = View.inflate(getActivity(), R.layout.person_upto_about, null);

        ImageView profile = (ImageView) personAbout.findViewById(R.id.profile);

        if(mPerson != null) {
            RealmResults<Update> recentUpdates = team.realm.where(Update.class)
                    .equalTo("person.id", mPerson.getId())
                    .findAll();
            recentUpdates.sort("date", false);
            updateList.setAdapter(new PersonUptoAdapter(getActivity(), recentUpdates));

            Picasso.with(getActivity())
                    .load(mPerson.getImageUrlForSize((int) Util.px(512)))
                    .placeholder(R.color.spacer)
                    .into(profile);

            ((TextView) personAbout.findViewById(R.id.info_followers)).setText(Long.toString(mPerson.getInfoFollowers()));
            ((TextView) personAbout.findViewById(R.id.info_following)).setText(Long.toString(mPerson.getInfoFollowing()));
            ((TextView) personAbout.findViewById(R.id.info_hosted)).setText(Long.toString(mPerson.getInfoHosted()));

            personAbout.findViewById(R.id.followers_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.showFollowers(mPerson);
                }
            });

            personAbout.findViewById(R.id.following_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.showFollowing(mPerson);
                }
            });

            if(mPerson.getAbout().isEmpty()) {
                personAbout.findViewById(R.id.about).setVisibility(View.GONE);
            }
            else {
                personAbout.findViewById(R.id.about).setVisibility(View.VISIBLE);
                ((TextView) personAbout.findViewById(R.id.about)).setText(mPerson.getAbout());
            }

            TextView actionButton = (TextView) personAbout.findViewById(R.id.action_button);

            Follow follow = team.realm.where(Follow.class)
                    .equalTo("person.id", team.auth.getUser())
                    .equalTo("following.id", mPerson.getId())
                    .findFirst();

            if(follow != null || mPerson.getId().equals(team.auth.getUser())) {
                actionButton.setVisibility(View.GONE);
            }
            else {
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setText(String.format(getActivity().getString(R.string.follow_person), mPerson.getFirstName()));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.action.followPerson(mPerson);
                    }
                });
            }
        }

        updateList.addHeaderView(personAbout);
        updateList.addFooterView(new View(getActivity()));

        updateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //team.action.openUpdate((Update) updateList.getAdapter().getItem(position));
            }
        });

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
        if(getActivity() == null || mPerson == null)
            return;

        team.api.get(String.format(Config.PATH_PEOPLE_ID, mPerson.getId()), new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.put(com.queatz.snappy.things.Person.class, response);
                update();

                mRefresh.setRefreshing(false);
            }

            @Override
            public void fail(String response) {
                mRefresh.setRefreshing(false);
            }
        });
    }

    public void update() {

    }
}