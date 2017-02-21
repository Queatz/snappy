package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.FeedAdapter;
import com.queatz.snappy.adapter.PeopleNearHereAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Here;
import com.queatz.snappy.team.OnScrollActions;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.EditText;
import com.queatz.snappy.ui.OnBackPressed;
import com.queatz.snappy.ui.RevealAnimation;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;

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
public class PartiesSlide extends MapSlide implements com.queatz.snappy.team.Location.LocationAvailabilityCallback, RealmChangeListener<DynamicRealm>, OnBackPressed {
    Team team;

    SwipeRefreshLayout mRefresh;
    ListView mList;
    View emptyView;

    private boolean layoutsShown = true;
    private boolean peopleNearbyShown = false;

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

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                emptyView.setMinimumHeight(view.getMeasuredHeight());
            }
        });

        setupTopLayout(view);

        mList = (ListView) view.findViewById(R.id.list);
        mList.addHeaderView(emptyView, null, false);
        mList.addFooterView(new View(getActivity()));

        emptyView.setClickable(true);
        emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                getChildFragmentManager().findFragmentById(R.id.map).getView().dispatchTouchEvent(motionEvent);
                return true;
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

        setupButtomLayout(view);

        update();

        mRefresh.post(new Runnable() {
            @Override
            public void run() {
                mRefresh.setRefreshing(true);
            }
        });

        refresh();

        team.location.addLocationAvailabilityCallback(this);

        view.post(new Runnable() {
            @Override
            public void run() {
                scroll();
            }
        });

        initMap(view);

        return view;
    }

    private void setupTopLayout(View view) {
        final View topLayout = view.findViewById(R.id.topLayout);

        topLayout.findViewById(R.id.peopleNearby).setVisibility(View.GONE);
        topLayout.findViewById(R.id.peopleNearbyText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = topLayout.findViewById(R.id.peopleNearbyListHolder);

                if (view.getVisibility() == View.GONE) {
                    RevealAnimation.expand(view);
                    peopleNearbyShown = true;
                } else {
                    RevealAnimation.collapse(view);
                    peopleNearbyShown = false;
                }
            }
        });

    }

    private void setupButtomLayout(View view) {
        final View bottomLayout = view.findViewById(R.id.bottomLayout);

        DynamicRealmObject person = team.auth.me();

        ImageView profile = (ImageView) bottomLayout.findViewById(R.id.profile);

        if(person != null) {
            Picasso.with(team.context)
                    .load(Functions.getImageUrlForSize(person, (int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into(profile);
        }

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (team.auth.me() == null) {
                    return;
                }

                team.action.openProfile(getActivity(), team.auth.me());
            }
        });

        Util.setOnScrollActions(mList, new OnScrollActions() {
            @Override
            public void up() {
                toggleLayouts(true);
            }

            @Override
            public void down() {
                toggleLayouts(false);

            }
        });

        final EditText whatsUp = (EditText) bottomLayout.findViewById(R.id.whatsUp);
        ImageButton sendButton = (ImageButton) bottomLayout.findViewById(R.id.sendButton);


        whatsUp.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_GO == actionId) {
                    want(whatsUp);
                }

                return false;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                want(whatsUp);
            }
        });
    }

    private void toggleLayouts(boolean show) {
        layoutsShown = show;

        if (getView() == null) {
            return;
        }

        final View bottomLayout = getView().findViewById(R.id.bottomLayout);
        final View topLayout = getView().findViewById(R.id.topLayout);

        if (show) {
            bottomLayout.animate()
                    .translationY(0)
                    .setDuration(225)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            topLayout.animate()
                    .translationY(0)
                    .setDuration(225)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        } else {
            bottomLayout.animate()
                    .translationY(bottomLayout.getMeasuredHeight())
                    .setDuration(195)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            topLayout.animate()
                    .translationY(-topLayout.getMeasuredHeight())
                    .setDuration(195)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void want(EditText whatsUp) {
        String text = whatsUp.getText().toString().trim();

        if (text.isEmpty()) {
            return;
        }

        team.action.want(text);
        whatsUp.setText("");
        team.view.keyboard(whatsUp, false);
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

    private void updateBanner(RealmList<DynamicRealmObject> people, RealmList<DynamicRealmObject> locations) {
        if(getActivity() == null || getView() == null)
            return;

        View peopleNearby = getView().findViewById(R.id.topLayout).findViewById(R.id.peopleNearby);

        if(people.size() < 1) {
            if(peopleNearby.getVisibility() != View.GONE) {
                RevealAnimation.collapse(peopleNearby);
            }

            return;
        }

        String locationName;

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
                    .notEqualTo("person.id", team.auth.getUser())
                    .findAllSorted(Thing.PRICE, Sort.ASCENDING);

            RealmResults<DynamicRealmObject> queryUpdates = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "update")
                    .notEqualTo("source.id", team.auth.getUser())
                    .findAllSorted(Thing.DATE, Sort.DESCENDING);

            final ArrayList<RealmResults> list = new ArrayList<>();
            list.add(queryParties);
            list.add(queryOffers);
            list.add(queryUpdates);
            mList.setAdapter(new FeedAdapter(getActivity(), list));
        }

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
                    } else if ("hub".equals(thing.getString(Thing.KIND))) {
                        locations.add(thing);
                    } else if("party".equals(thing.getString(Thing.KIND))) {
                        team.api.get(Config.PATH_EARTH + "/" + thing.getString(Thing.ID), new Api.Callback() {
                            @Override
                            public void success(String response) {
                                // Do this to update party joins
                                team.things.put(response);
                            }

                            @Override
                            public void fail(String response) {

                            }
                        });
                    }
                }

                updateBanner(people, locations);
                update();
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        if (super.onBackPressed()) {
            return true;
        }

        if (!layoutsShown) {
            toggleLayouts(true);
            return true;
        }

        if (peopleNearbyShown && getView() != null) {
            getView().findViewById(R.id.topLayout).findViewById(R.id.peopleNearbyText).callOnClick();
            return true;
        }

        if (mList.getFirstVisiblePosition() == 0 && mList.getChildAt(0).getTop() > -mList.getMeasuredHeight() / 4) {
            scroll();
            return true;
        }

        return false;
    }

    private void scroll() {
        mList.smoothScrollToPositionFromTop(0, -mList.getMeasuredHeight() / 2);
    }
}
