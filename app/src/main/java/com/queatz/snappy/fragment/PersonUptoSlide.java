package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Offer;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Update;
import com.queatz.snappy.ui.RevealAnimation;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.ui.TimeSlider;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonUptoSlide extends Fragment {
    Team team;
    com.queatz.snappy.things.Person mPerson;
    View personAbout;
    FloatingActionButton mFloatingAction;
    RealmChangeListener mChangeListener = null;
    boolean mShowOffers;

    public void setPerson(com.queatz.snappy.things.Person person) {
        mPerson = person;
    }

    SwipeRefreshLayout mRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = ((MainApplication) getActivity().getApplication()).team;

        mChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                update();
            }
        };

        team.realm.addChangeListener(mChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mChangeListener != null) {
            team.realm.removeChangeListener(mChangeListener);
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
            RealmResults<Update> recentUpdates = team.realm.where(Update.class)
                    .equalTo("person.id", mPerson.getId())
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

        if(team.auth.getUser() != null && team.auth.getUser().equals(mPerson.getId())) {
            mFloatingAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.offerSomething(getActivity());
                }
            });
        } else {
            mFloatingAction.setVisibility(View.GONE);
        }

        return view;
    }

    private void updateBanner() {
        if(mPerson == null || getActivity() == null)
            return;

        RealmResults<Offer> offers = team.realm.where(Offer.class).equalTo("person.id", mPerson.getId()).findAllSorted("price", Sort.ASCENDING);

        View offersView = personAbout.findViewById(R.id.offers);

        boolean itsMe = team.auth.getUser().equals(mPerson.getId());

        if(offers.size() < 1 && !itsMe) {
            if(offersView.getVisibility() != View.GONE) {
                RevealAnimation.collapse(offersView);
            }

            return;
        }

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

        team.api.get(String.format(Config.PATH_PEOPLE_ID, mPerson.getId()), new Api.Callback() {
            @Override
            public void success(String response) {
                if (response == null) {
                    return;
                }

                if(mPerson == null) {
                    return;
                }

                List<Offer> previousOffers = team.realm.where(Offer.class).equalTo("person.id", mPerson.getId()).findAll();
                Person add = team.things.put(com.queatz.snappy.things.Person.class, response);
                team.things.diff(previousOffers, add.getOffers());

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
                    .load(mPerson.getImageUrlForSize((int) Util.px(512)))
                    .placeholder(R.color.deepdarkred)
                    .into(profile);

            ((TextView) personAbout.findViewById(R.id.info_followers)).setText(Long.toString(mPerson.getInfoFollowers()));
            ((TextView) personAbout.findViewById(R.id.info_following)).setText(Long.toString(mPerson.getInfoFollowing()));

            personAbout.findViewById(R.id.hosted_button).setVisibility(mPerson.getCreated() != null ? View.VISIBLE : View.GONE);

            TextView created = (TextView) personAbout.findViewById(R.id.info_hosted);

            if (mPerson.getCreated() != null) {
                created.setText(TimeUtil.agoDate(mPerson.getCreated(), false));
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

            if(team.auth.getUser() != null && team.auth.getUser().equals(mPerson.getId())) {
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

            if(mPerson.getAbout() == null || mPerson.getAbout().isEmpty()) {
                if(team.auth.getUser().equals(mPerson.getId())) {
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
                about.setText(mPerson.getAbout());
            }

            Button actionButton = (Button) personAbout.findViewById(R.id.action_button);

            Follow follow = null;

            if(team.auth.getUser() != null) {
                follow = team.realm.where(Follow.class)
                        .equalTo("source.id", team.auth.getUser())
                        .equalTo("target.id", mPerson.getId())
                        .findFirst();
            }

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
    }
}
