package com.queatz.snappy.fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.queatz.branch.Branch;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.adapter.FeedAdapter;
import com.queatz.snappy.adapter.ModeAdapter;
import com.queatz.snappy.adapter.OpenMessagesAction;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.TeamFragment;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;
import com.queatz.snappy.team.actions.AddModeAction;
import com.queatz.snappy.team.actions.AuthenticatedAction;
import com.queatz.snappy.team.actions.BackThingAction;
import com.queatz.snappy.team.actions.ChangeAboutAction;
import com.queatz.snappy.team.actions.OfferSomethingAction;
import com.queatz.snappy.team.actions.ShowBackersAction;
import com.queatz.snappy.team.actions.ShowBackingAction;
import com.queatz.snappy.team.actions.UpdateThings;
import com.queatz.snappy.team.actions.ViewModeAction;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.ui.slidescreen.SlideScreen;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;
import com.queatz.snappy.util.TimeUtil;

import java.util.ArrayList;

import io.realm.DynamicRealmObject;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonUptoSlide extends TeamFragment {
    private Team team;
    private DynamicRealmObject mPerson;
    private View personAbout;
    private TextView socialMode;
    private FloatingActionButton mFloatingAction;
    private RealmChangeListener<DynamicRealmObject> mChangeListener = null;
    private ListView updateList;
    private int topGlassHeight;
    private TextView aboutText;

    @Override
    public void to(Branch<ActivityContext> branch) {
        Branch.from((ActivityContext) getActivity()).to(branch);
    }

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
        updateList = view.findViewById(R.id.updateList);

        socialMode = view.findViewById(R.id.socialMode);

        personAbout = View.inflate(getActivity(), R.layout.person_upto_about, null);
        aboutText = personAbout.findViewById(R.id.about);

        final View topGlass = personAbout.findViewById(R.id.topGlass);

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (topGlassHeight != view.getMeasuredHeight()) {
                    topGlassHeight = view.getMeasuredHeight();
                    topGlass.setMinimumHeight(topGlassHeight);
                }
            }
        });

        updateList.addHeaderView(personAbout);
        updateList.addFooterView(new View(getActivity()));

        if(mPerson != null) {
            RealmResults<DynamicRealmObject> offers = team.realm.where("Thing")
                    .equalTo(Thing.KIND, ThingKinds.MEMBER)
                    .equalTo(Thing.TARGET + "." + Thing.ID, mPerson.getString(Thing.ID))
                    .sort(Thing.SOURCE + "." + Thing.DATE, Sort.DESCENDING)
                    .findAll();

            final ArrayList<RealmResults<DynamicRealmObject>> list = new ArrayList<>();
            list.add(offers);

            updateList.setAdapter(new FeedAdapter(getActivity(), list));
        }

        update(view);

        refresh();

        mFloatingAction = view.findViewById(R.id.floatingAction);

        final boolean itsMe = mPerson != null && team.auth.getUser() != null && team.auth.getUser().equals(mPerson.getString(Thing.ID));

        if(itsMe) {
            mFloatingAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    to(new OfferSomethingAction());
                }
            });

            Util.attachFAB(mFloatingAction, updateList);
        } else {
            mFloatingAction.setVisibility(View.GONE);
        }

        socialMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || !(getActivity() instanceof Person)) {
                    return;
                }

                SlideScreen slideScreen = ((Person) getActivity()).getSlideScreen();

                if (!itsMe) {
                    ((PersonMessagesSlide) slideScreen.getSlideFragment(Person.SLIDE_MESSAGES)).setMessagePrefill(getString(R.string.hey_name, mPerson.getString(Thing.FIRST_NAME)));
                }

                slideScreen.setSlide(Person.SLIDE_MESSAGES);
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
                to(new UpdateThings(response).when(Boolean.class, new Branch<Boolean>() {
                    @Override
                    protected void execute() {
                        if (me()) {
                            update(getView());
                        }
                    }
                }));
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


            ImageView profile = view.findViewById(R.id.profile);

            Images.with(getActivity())
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
                    to(new ShowBackersAction(mPerson));
                }
            });

            personAbout.findViewById(R.id.following_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    to(new ShowBackingAction(mPerson));
                }
            });

            Button messageButton = ((Button) personAbout.findViewById(R.id.action_message));
            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    to(new OpenMessagesAction(mPerson));
                }
            });

            if(team.auth.getUser() != null && team.auth.getUser().equals(mPerson.getString(Thing.ID))) {
                messageButton.setText(getString(R.string.view_settings));

                aboutText.setTextIsSelectable(false);

                aboutText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        to(new ChangeAboutAction());
                    }
                });
            }
            else {
                messageButton.setText(getString(R.string.message_person, mPerson.getString(Thing.FIRST_NAME)));
                aboutText.setTextIsSelectable(true);
            }

            if(mPerson.getString(Thing.ABOUT) == null || mPerson.getString(Thing.ABOUT).isEmpty()) {
                to(new AuthenticatedAction() {
                    @Override
                    public void whenAuthenticated() {
                        if(getUser().get(Thing.ID).equals(mPerson.getString(Thing.ID))) {
                            aboutText.setVisibility(View.VISIBLE);
                            aboutText.setTextColor(getResources().getColor(R.color.clickable));
                            aboutText.setText(R.string.what_are_you_into);
                        }
                        else {
                            aboutText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void otherwise() {
                        aboutText.setVisibility(View.GONE);
                    }
                });
            } else {
                aboutText.setVisibility(View.VISIBLE);
                aboutText.setTextColor(getResources().getColor(R.color.text));
                aboutText.setText(mPerson.getString(Thing.ABOUT));
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
                        to(new BackThingAction(mPerson));
                    }
                });
            }
        }

        updateModes();
    }

    private void updateModes() {
        final ListView modesList = personAbout.findViewById(R.id.modesList);
        final Button addModeButton = personAbout.findViewById(R.id.action_add_mode);
        final TextView noModes = personAbout.findViewById(R.id.noModes);

        noModes.setText(getString(R.string.person_has_not_turned_on_any_modes_yet, mPerson.getString(Thing.FIRST_NAME)));

        boolean isMe = mPerson.getString(Thing.ID).equals(team.auth.getUser());

        if (isMe) {
            addModeButton.setVisibility(View.VISIBLE);
        } else {
            addModeButton.setVisibility(View.GONE);
        }

        addModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                to(new AddModeAction());
            }
        });

        if (modesList.getAdapter() == null) {
            RealmResults<DynamicRealmObject> q = team.realm.where("Thing")
                    .equalTo(Thing.KIND, ThingKinds.MEMBER)
                    .equalTo("target.id", mPerson.getString(Thing.ID))
                    .equalTo("source.kind", ThingKinds.MODE)
                    .findAll();

            q.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<DynamicRealmObject>>() {
                @Override
                public void onChange(RealmResults<DynamicRealmObject> dynamicRealmObjects, OrderedCollectionChangeSet orderedCollectionChangeSet) {
                    noModes.setVisibility(dynamicRealmObjects.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });

            noModes.setVisibility(q.isEmpty() ? View.VISIBLE : View.GONE);


            modesList.setAdapter(new ModeAdapter(getActivity(), q));

            modesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    to(new ViewModeAction(((DynamicRealmObject) modesList.getAdapter().getItem(i)).getObject(Thing.SOURCE)));
                }
            });
        }
    }

    private void scroll() {
        updateList.smoothScrollToPositionFromTop(0, -updateList.getMeasuredHeight() / 2);
    }
}
