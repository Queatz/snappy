package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import com.queatz.snappy.things.Bounty;
import com.queatz.snappy.things.Location;
import com.queatz.snappy.things.Offer;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Quest;
import com.queatz.snappy.ui.EditText;
import com.queatz.snappy.ui.RevealAnimation;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.ui.TimeSlider;

import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 10/19/14.
 */
public class PartiesSlide extends Fragment implements com.queatz.snappy.team.Location.LocationAvailabilityCallback, RealmChangeListener {
    Team team;

    SwipeRefreshLayout mRefresh;
    ListView mList;
    View emptyView;
    Object mContextObject;

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

        final TimeSlider priceSlider = (TimeSlider) emptyView.findViewById(R.id.price);
        final EditText query = (EditText) emptyView.findViewById(R.id.query);

        // Work around Android bug
        query.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_CANCEL == event.getAction()) {
                    return true;
                }

                return false;
            }
        });

        priceSlider.setPercent(.25f);
        priceSlider.setTextCallback(new TimeSlider.TextCallback() {
            @Override
            public String getText(float percent) {
                int price = getPrice(percent);

                return getString(R.string.for_price, "$" + Integer.toString(price));
            }
        });

        emptyView.findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.addExperience(query.getText().toString(), -getPrice(priceSlider.getPercent()), "");
                query.setText("");
                team.action.openProfile(getActivity(), team.auth.me());
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

    private int getPrice(float percent) {
        return (int) ((percent * 5) + 1) * 10;
    }

    private void updateBanner(RealmList<Person> people, RealmList<com.queatz.snappy.things.Location> locations) {
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
            locationName = locations.get(0).getName();
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
                Person person = ((PeopleNearHereAdapter) parent.getAdapter()).getItem(position);

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

            RealmResults<Party> queryParties = team.realm.where(Party.class)
                        .greaterThan("date", new Date(new Date().getTime() - 1000 * 60 * 60))
                    .beginGroup()
                        .equalTo("full", false).or()
                        .equalTo("people.person.id", me).or()
                        .equalTo("host.id", me)
                    .endGroup()
                    .findAllSorted("date", Sort.ASCENDING);

            RealmResults<Quest> queryQuests = team.realm.where(Quest.class).greaterThan("opened", new Date(new Date().getTime() - 1000L * 60 * 60 * 24 * 30))
                    .notEqualTo("status", Config.QUEST_STATUS_COMPLETE)
                    .beginGroup()
                    .equalTo("status", Config.QUEST_STATUS_OPEN)
                    .or()
                    .equalTo("team.id", team.auth.getUser())
                    .or()
                    .equalTo("host.id", team.auth.getUser())
                    .endGroup()
                    .findAllSorted("opened", Sort.DESCENDING);

            RealmResults<Offer> queryOffers = team.realm.where(Offer.class).notEqualTo("person.id", team.auth.getUser()).findAllSorted("price", Sort.ASCENDING);

            final ArrayList<RealmResults> list = new ArrayList<>();
            list.add(queryParties);
            list.add(queryQuests);
            list.add(queryOffers);
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
            public void onSuccess(RealmList<Person> people, RealmList<Location> locations, RealmList<Party> parties, RealmList<Bounty> bounties, RealmList<Quest> quests, RealmList<Offer> offers) {
                if (locations != null && people != null) {
                    updateBanner(people, locations);
                }

                if (parties != null) {
                    update();
                }
            }
        });
    }
}
