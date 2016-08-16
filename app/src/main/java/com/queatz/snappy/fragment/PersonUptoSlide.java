package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.OfferAdapter;
import com.queatz.snappy.adapter.PersonUptoAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.RevealAnimation;
import com.queatz.snappy.ui.SlideScreen;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.realm.DynamicRealmObject;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonUptoSlide extends Fragment {
    Team team;
    DynamicRealmObject mPerson;
    View personAbout;
    FloatingActionButton mFloatingAction;
    RealmChangeListener<DynamicRealmObject> mChangeListener = null;
    boolean mShowOffers;

    public void setPerson(DynamicRealmObject person) {
        mPerson = person;
    }

    SwipeRefreshLayout mRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = ((MainApplication) getActivity().getApplication()).team;

        mChangeListener = new RealmChangeListener<DynamicRealmObject>() {
            @Override
            public void onChange(DynamicRealmObject object) {
                update();
            }
        };

        mPerson.addChangeListener(mChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mChangeListener != null && mPerson != null) {
            mPerson.removeChangeListener(mChangeListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.person_upto, container, false);

        final ListView updateList = ((ListView) view.findViewById(R.id.updateList));

        personAbout = View.inflate(getActivity(), R.layout.person_upto_about, null);
        personAbout.findViewById(R.id.offers).setVisibility(View.GONE);

        updateList.addHeaderView(personAbout);
        updateList.addFooterView(new View(getActivity()));

        if(mPerson != null) {
            RealmResults<DynamicRealmObject> recentUpdates = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "update")
                    .equalTo("source.id", mPerson.getString(Thing.ID))
                    .findAllSorted("date", Sort.DESCENDING);
            updateList.setAdapter(new PersonUptoAdapter(getActivity(), recentUpdates));
        }

        update(true);

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

        mFloatingAction = (FloatingActionButton) view.findViewById(R.id.floatingAction);

        final boolean itsMe = mPerson != null && team.auth.getUser() != null && team.auth.getUser().equals(mPerson.getString(Thing.ID));

        if(itsMe) {
            mFloatingAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.offerSomething(getActivity());
                }
            });
        } else {
            mFloatingAction.setVisibility(View.GONE);
        }


        view.findViewById(R.id.socialMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlideScreen slideScreen = (SlideScreen) getActivity().findViewById(R.id.person_content);

                if (!itsMe) {
                    ((PersonMessagesSlide) slideScreen.getSlideFragment(1)).setMessagePrefill("Hey!");
                }

                slideScreen.setSlide(1);
            }
        });

        return view;
    }

    private void updateBanner() {
        if(mPerson == null || getActivity() == null)
            return;

        RealmResults<DynamicRealmObject> offers = team.realm.where("Thing")
                .equalTo(Thing.KIND, "offer")
                .equalTo("person.id", mPerson.getString(Thing.ID))
                .findAllSorted("price", Sort.ASCENDING);

        View offersView = personAbout.findViewById(R.id.offers);

        boolean itsMe = team.auth.getUser().equals(mPerson.getString(Thing.ID));

        if(offers.size() < 1 && !itsMe) {
            if(offersView.getVisibility() != View.GONE) {
                RevealAnimation.collapse(offersView);
            }

            return;
        }

        // TODO find better way to sort with "Ask for price" offers appearing at the end


        ListView offersList = (ListView) offersView.findViewById(R.id.offersList);

        OfferAdapter offersAdapter = new OfferAdapter(getActivity(), offers);

        offersList.setAdapter(offersAdapter);

        if(offersView.getVisibility() == View.GONE) {
            RevealAnimation.expand(offersView);
        }
    }

    public void refresh() {
        if(getActivity() == null || mPerson == null)
            return;

        team.api.get(Config.PATH_EARTH + "/" + mPerson.getString(Thing.ID), new Api.Callback() {
            @Override
            public void success(String response) {
                if (response == null) {
                    return;
                }

                if(mPerson == null) {
                    return;
                }

                List<DynamicRealmObject> previousOffers = team.realm.where("Thing")
                        .equalTo(Thing.KIND, "offer")
                        .equalTo("source.id", mPerson.getString(Thing.ID))
                        .findAll();
                DynamicRealmObject add = team.things.put(response);
                team.things.diff(previousOffers, add.getList(Thing.OFFERS));

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
        update(false);
    }

    private void update(boolean initial) {
        if(getActivity() == null) {
            return;
        }

        personAbout.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBanner();

                if (mShowOffers) {
                    mShowOffers = false;
                    personAbout.findViewById(R.id.offers).callOnClick();
                }
            }
        }, initial ? 500 : 0);

        ImageView profile = (ImageView) personAbout.findViewById(R.id.profile);

        if(mPerson != null) {
            profile.setTag(mPerson);

            if(getActivity() != null) {
                getActivity().registerForContextMenu(profile);
            }
            
            Picasso.with(getActivity())
                    .load(Functions.getImageUrlForSize(mPerson, (int) Util.px(512)))
                    .placeholder(R.color.deepdarkred)
                    .into(profile);

            ((TextView) personAbout.findViewById(R.id.info_followers)).setText(Long.toString(mPerson.getInt(Thing.INFO_FOLLOWERS)));
            ((TextView) personAbout.findViewById(R.id.info_following)).setText(Long.toString(mPerson.getInt(Thing.INFO_FOLLOWING)));

            personAbout.findViewById(R.id.hosted_button).setVisibility(mPerson.getDate(Thing.CREATED_ON) != null ? View.VISIBLE : View.GONE);

            TextView created = (TextView) personAbout.findViewById(R.id.info_hosted);

            if (mPerson.getDate(Thing.CREATED_ON) != null) {
                created.setText(TimeUtil.agoDate(mPerson.getDate(Thing.CREATED_ON), false));
                created.setVisibility(View.VISIBLE);
            } else {
                created.setVisibility(View.GONE);
            }

            personAbout.findViewById(R.id.followers_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.showFollowers(getActivity(), mPerson);
                }
            });

            personAbout.findViewById(R.id.following_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.showFollowing(getActivity(), mPerson);
                }
            });

            TextView about = (TextView) personAbout.findViewById(R.id.about);

            if(team.auth.getUser() != null && team.auth.getUser().equals(mPerson.getString(Thing.ID))) {
                about.setTextIsSelectable(false);

                about.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.action.changeAbout(getActivity());
                    }
                });
            }
            else {
                about.setTextIsSelectable(true);
            }

            if(mPerson.getString(Thing.ABOUT) == null || mPerson.getString(Thing.ABOUT).isEmpty()) {
                if(team.auth.getUser().equals(mPerson.getString(Thing.ID))) {
                    about.setVisibility(View.VISIBLE);
                    about.setTextColor(getResources().getColor(R.color.clickable));
                    about.setText(R.string.what_are_you_into);
                }
                else {
                    about.setVisibility(View.GONE);
                }
            }
            else {
                about.setVisibility(View.VISIBLE);
                about.setTextColor(getResources().getColor(R.color.text));
                about.setText(mPerson.getString(Thing.ABOUT));
            }

            Button actionButton = (Button) personAbout.findViewById(R.id.action_button);

            DynamicRealmObject follow = null;

            if(team.auth.getUser() != null) {
                follow = team.realm.where("Thing")
                        .equalTo(Thing.KIND, "follow")
                        .equalTo("source.id", team.auth.getUser())
                        .equalTo("target.id", mPerson.getString(Thing.ID))
                        .findFirst();
            }

            if(follow != null || mPerson.getString(Thing.ID).equals(team.auth.getUser())) {
                actionButton.setVisibility(View.GONE);
            }
            else {
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setText(String.format(getActivity().getString(R.string.follow_person), mPerson.getString(Thing.FIRST_NAME)));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.action.followPerson(mPerson);
                    }
                });
            }
        }
    }
}
