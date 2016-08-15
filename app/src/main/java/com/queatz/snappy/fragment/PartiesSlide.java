package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.FeedAdapter;
import com.queatz.snappy.adapter.PeopleNearHereAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Here;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.RevealAnimation;
import com.queatz.snappy.ui.TextView;

import java.util.ArrayList;
import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 10/19/14.
 */
public class PartiesSlide extends Fragment implements com.queatz.snappy.team.Location.LocationAvailabilityCallback, RealmChangeListener<DynamicRealm> {
    Team team;

    SwipeRefreshLayout mRefresh;
    ListView mList;
    View emptyView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;
        team.realm.addChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parties, container, false);
        emptyView = View.inflate(getActivity(), R.layout.parties_empty, null);

        emptyView.findViewById(R.id.peopleNearby).setVisibility(View.GONE);
        emptyView.findViewById(R.id.peopleNearby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = emptyView.findViewById(R.id.peopleNearbyListHolder);

                if (view.getVisibility() == View.GONE)
                    RevealAnimation.expand(view);
                else
                    RevealAnimation.collapse(view);
            }
        });

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

        View floatingAction = view.findViewById(R.id.floatingAction);
        floatingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.offerSomething(getActivity());
            }
        });

        update();

        mRefresh.post(new Runnable() {
            @Override
            public void run() {
                mRefresh.setRefreshing(true);
            }
        });

        refresh();

        team.location.addLocationAvailabilityCallback(this);

        return view;
    }

    @Override
    public void onDestroy() {
        team.realm.removeChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onChange(DynamicRealm realm) {
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

    private int getPrice(float percent) {
        return (int) ((percent * 5) + 1) * 10;
    }

    private void updateBanner(RealmList<DynamicRealmObject> people, RealmList<DynamicRealmObject> locations) {
        if(getActivity() == null)
            return;

        View peopleNearby = emptyView.findViewById(R.id.peopleNearby);

        if(people.size() < 1) {
            if(peopleNearby.getVisibility() != View.GONE) {
                RevealAnimation.collapse(peopleNearby);
            }

            return;
        }

        String locationName = null;

        if(locations.size() > 0) {
            locationName = locations.get(0).getString(Thing.NAME);
        }
        else {
            locationName = getString(R.string.here);
        }

        View peopleNearbyListHolder = peopleNearby.findViewById(R.id.peopleNearbyListHolder);
        GridView peopleNearbyList = (GridView) peopleNearby.findViewById(R.id.peopleNearbyList);

        PeopleNearHereAdapter peopleNearHereAdapter = new PeopleNearHereAdapter(getActivity(), people);

        peopleNearbyList.setAdapter(peopleNearHereAdapter);

        peopleNearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DynamicRealmObject person = ((PeopleNearHereAdapter) parent.getAdapter()).getItem(position);

                if (person != null) {
                    team.action.openProfile(getActivity(), person);
                }
            }
        });

        if(peopleNearby.getVisibility() == View.GONE) {
            peopleNearbyListHolder.setVisibility(View.GONE);
            RevealAnimation.expand(peopleNearby, 500);
        }

        ((TextView) peopleNearby.findViewById(R.id.peopleNearbyText)).setText(
                getResources().getQuantityString(R.plurals.people_near_place, people.size(), people.size(), locationName)
        );
    }

    public void update() {
        if(getActivity() == null)
            return;

        if(mList.getAdapter() == null) {
            String me = team.auth.getUser();

            RealmResults<DynamicRealmObject> queryParties = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "party")
                        .greaterThan("date", new Date(new Date().getTime() - 1000 * 60 * 60))
                    .beginGroup()
                        .equalTo("full", false).or()
                        .equalTo("joins.source.id", me).or()
                        .equalTo("host.id", me)
                    .endGroup()
                    .findAllSorted("date", Sort.ASCENDING);

            RealmResults<DynamicRealmObject> queryOffers = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "offer")
                    .notEqualTo("source.id", team.auth.getUser())
                    .isNotNull("price")
                    .findAllSorted("price", Sort.ASCENDING);

            RealmResults<DynamicRealmObject> queryOffersUpmarket = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "offer")
                    .notEqualTo("source.id", team.auth.getUser())
                    .isNull("price")
                    .findAll();

            final ArrayList<RealmResults> list = new ArrayList<>();
            list.add(queryParties);
            list.add(queryOffers);
            list.add(queryOffersUpmarket);
            mList.setAdapter(new FeedAdapter(getActivity(), list));
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

        team.here.update(getActivity(), mRefresh, new Here.Callback() {
            @Override
            public void onSuccess(RealmList<DynamicRealmObject> things) {
                RealmList<DynamicRealmObject> people = new RealmList<>();
                RealmList<DynamicRealmObject> locations = new RealmList<>();

                for (DynamicRealmObject thing : things) {
                    if ("person".equals(thing.getString(Thing.KIND))) {
                        people.add(thing);
                    } else if ("location".equals(thing.getString(Thing.KIND))) {
                        locations.add(thing);
                    }
                }

                updateBanner(people, locations);
                update();
            }
        });
    }
}
