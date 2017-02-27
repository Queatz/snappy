package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.FeedAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.SlideScreen;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
    TextView socialMode;
    FloatingActionButton mFloatingAction;
    RealmChangeListener<DynamicRealmObject> mChangeListener = null;
    ListView updateList;

    public void setPerson(DynamicRealmObject person) {
        mPerson = person;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Config.EXTRA_PERSON_ID, mPerson.getString(Thing.ID));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;

        if (mPerson == null && savedInstanceState != null && savedInstanceState.containsKey(Config.EXTRA_PERSON_ID)) {
            String personId = savedInstanceState.getString(Config.EXTRA_PERSON_ID);

            mPerson = team.realm.where("Thing").equalTo(Thing.ID, personId).findFirst();

            if (mPerson == null) {
                return;
            }
        }

        mChangeListener = new RealmChangeListener<DynamicRealmObject>() {
            @Override
            public void onChange(DynamicRealmObject object) {
                update(getView());
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
        updateList = ((ListView) view.findViewById(R.id.updateList));

        socialMode = (TextView) view.findViewById(R.id.socialMode);

        personAbout = View.inflate(getActivity(), R.layout.person_upto_about, null);

        final View topGlass = personAbout.findViewById(R.id.topGlass);

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                topGlass.setMinimumHeight(view.getMeasuredHeight());
            }
        });

        updateList.addHeaderView(personAbout);
        updateList.addFooterView(new View(getActivity()));

        if(mPerson != null) {
            RealmResults<DynamicRealmObject> offers = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "offer")
                    .equalTo("person.id", mPerson.getString(Thing.ID))
                    .findAllSorted("price", Sort.ASCENDING);

            RealmResults<DynamicRealmObject> recentUpdates = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "update")
                    .equalTo("person.id", mPerson.getString(Thing.ID))
                    .equalTo("target.id", mPerson.getString(Thing.ID))
                    .findAllSorted("date", Sort.DESCENDING);

            final ArrayList<RealmResults> list = new ArrayList<>();
            list.add(offers);
            list.add(recentUpdates);

            updateList.setAdapter(new FeedAdapter(getActivity(), list));
        }

        update(view);

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

            Util.attachFAB(mFloatingAction, updateList);
        } else {
            mFloatingAction.setVisibility(View.GONE);
        }

        TextView socialMode = (TextView) view.findViewById(R.id.socialMode);

        socialMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlideScreen slideScreen = (SlideScreen) getActivity().findViewById(R.id.person_content);

                if (!itsMe) {
                    ((PersonMessagesSlide) slideScreen.getSlideFragment(1)).setMessagePrefill("Hey!");
                }

                slideScreen.setSlide(1);
            }
        });

        view.post(new Runnable() {
            @Override
            public void run() {
                scroll();
            }
        });

        scroll();

        return view;
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

                update(getView());
            }

            @Override
            public void fail(String response) {

            }
        });
    }

    private void update(View view) {
        if(getActivity() == null) {
            return;
        }

        if(mPerson != null) {
            if (socialMode != null) {
                String social = mPerson.getString(Thing.SOCIAL_MODE);

                if (social != null) {
                    socialMode.setVisibility(View.VISIBLE);
                    socialMode.setText(getString(R.string.social_mode_set, social));

                    switch (social) {
                        case Config.SOCIAL_MODE_ON:
                        case Config.SOCIAL_MODE_FRIENDS:
                            socialMode.setTextColor(getResources().getColor(R.color.green));
                            break;
                        case Config.SOCIAL_MODE_OFF:
                            socialMode.setTextColor(getResources().getColor(R.color.gray));
                            break;
                    }
                } else {
                    socialMode.setVisibility(View.GONE);
                }
            }

            final View topGlass = personAbout.findViewById(R.id.topGlass);
            topGlass.setClickable(true);
            topGlass.setTag(mPerson);
            getActivity().registerForContextMenu(topGlass);


            ImageView profile = (ImageView) view.findViewById(R.id.profile);

            Picasso.with(getActivity())
                    .load(Functions.getImageUrlForSize(mPerson, (int) Util.px(512)))
                    .into(profile);

            ((TextView) personAbout.findViewById(R.id.name)).setText(Functions.getFullName(mPerson));

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

            TextView proximity = (TextView) personAbout.findViewById(R.id.proximity);

            if (!mPerson.isNull(Thing.INFO_DISTANCE)) {
                proximity.setText(Util.getProximityText(mPerson));
                proximity.setVisibility(View.VISIBLE);
            } else {
                proximity.setVisibility(View.GONE);
            }

            Button actionButton = (Button) personAbout.findViewById(R.id.action_button);

            DynamicRealmObject follow = null;

            if(team.auth.getUser() != null) {
                follow = team.realm.where("Thing")
                        .equalTo(Thing.KIND, "follower")
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

    private void scroll() {
        updateList.smoothScrollToPositionFromTop(0, -updateList.getMeasuredHeight() / 2);
    }
}
