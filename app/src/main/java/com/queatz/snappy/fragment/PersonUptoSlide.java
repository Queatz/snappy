package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.adapter.OfferAdapter;
import com.queatz.snappy.adapter.PersonUptoAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Offer;
import com.queatz.snappy.things.Update;
import com.queatz.snappy.ui.RevealAnimation;
import com.queatz.snappy.ui.SlideScreen;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.ui.TimeSlider;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonUptoSlide extends Fragment {
    Team team;
    com.queatz.snappy.things.Person mPerson;
    View personAbout;
    EditText describeExperience;
    EditText perUnit;
    RealmChangeListener mChangeListener = null;

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
        View view = inflater.inflate(R.layout.person_upto, container, false);

        final ListView updateList = ((ListView) view.findViewById(R.id.updateList));

        personAbout = View.inflate(getActivity(), R.layout.person_upto_about, null);

        personAbout.findViewById(R.id.offers).setVisibility(View.GONE);
        personAbout.findViewById(R.id.offers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = personAbout.findViewById(R.id.offersListHolder);

                if (view.getVisibility() == View.GONE)
                    RevealAnimation.expand(view);
                else
                    RevealAnimation.collapse(view);
            }
        });

        updateList.addHeaderView(personAbout);
        updateList.addFooterView(new View(getActivity()));

        if(mPerson != null) {
            if(team.auth.getUser().equals(mPerson.getId())) {
                ListView offersList = (ListView) personAbout.findViewById(R.id.offers).findViewById(R.id.offersList);

                final View newOffer = View.inflate(getActivity(), R.layout.new_offer, null);

                offersList.addFooterView(newOffer);

                final EditText experienceDetails = (EditText) newOffer.findViewById(R.id.details);
                final TimeSlider priceSlider = (TimeSlider) newOffer.findViewById(R.id.price);
                final Button addExperience = (Button) newOffer.findViewById(R.id.addExperience);

                describeExperience = experienceDetails;
                perUnit = (EditText) newOffer.findViewById(R.id.perWhat);

                priceSlider.setPercent(getFreePercent());
                priceSlider.setTextCallback(new TimeSlider.TextCallback() {
                    @Override
                    public String getText(float percent) {
                        int price = getPrice(percent);

                        if (price < 0) {
                            newOffer.setBackgroundResource(R.color.darkpurple);
                        } else {
                            newOffer.setBackgroundResource(R.color.darkgreen);
                        }

                        if (price == 0) {
                            return getString(R.string.free);
                        }

                        return  (price < 0 ? "-" : "") + "$" + Integer.toString(Math.abs(price));
                    }
                });

                addExperience.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.action.addExperience(experienceDetails.getText().toString(), getPrice(priceSlider.getPercent()), perUnit.getText().toString());
                        priceSlider.setPercent(getFreePercent());
                        experienceDetails.setText("");
                        perUnit.setText("");
                    }
                });
            }

            RealmResults<Update> recentUpdates = team.realm.where(Update.class)
                    .equalTo("person.id", mPerson.getId())
                    .findAllSorted("date", false);
            updateList.setAdapter(new PersonUptoAdapter(getActivity(), recentUpdates));
        }

        update(true);

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

    private int getPrice(float percent) {
        int price;

        if (Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled())) {
            price = (int) (percent * (Config.PAID_OFFER_PRICE_MAX - Config.PAID_OFFER_PRICE_MIN) + Config.PAID_OFFER_PRICE_MIN);
        } else {
            price = (int) (percent * (Config.FREE_OFFER_PRICE_MAX - Config.FREE_OFFER_PRICE_MIN) + Config.FREE_OFFER_PRICE_MIN);
        }

        if (Math.abs(price) < 200) {
            price = (int) Math.floor(price / 10) * 10;
        } else if (Math.abs(price) < 1000) {
            price = (int) Math.floor(price / 50) * 50;
        } else {
            price = (int) Math.floor(price / 100) * 100;
        }

        return price;
    }

    private float getFreePercent() {
        if (Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled())) {
            return (float) -Config.PAID_OFFER_PRICE_MIN / (float) (-Config.PAID_OFFER_PRICE_MIN + Config.PAID_OFFER_PRICE_MAX);
        } else {
            return (float) -Config.FREE_OFFER_PRICE_MIN / (float) (-Config.FREE_OFFER_PRICE_MIN + Config.FREE_OFFER_PRICE_MAX);
        }
    }

    private void updateBanner() {
        if(mPerson == null || getActivity() == null)
            return;

        RealmResults<Offer> offers = team.realm.where(Offer.class).equalTo("person.id", mPerson.getId()).findAllSorted("price", true);

        View offersView = personAbout.findViewById(R.id.offers);

        boolean itsMe = team.auth.getUser().equals(mPerson.getId());

        if(offers.size() < 1 && !itsMe) {
            if(offersView.getVisibility() != View.GONE) {
                RevealAnimation.collapse(offersView);
            }

            return;
        }

        if(describeExperience != null) {
            describeExperience.setHint(offers.size() < 1 ? R.string.describe_the_experience : R.string.describe_another_experience);
        }

        View offersListHolder = offersView.findViewById(R.id.offersListHolder);
        ListView offersList = (ListView) offersView.findViewById(R.id.offersList);

        OfferAdapter offersAdapter = new OfferAdapter(getActivity(), offers);

        offersList.setAdapter(offersAdapter);

        offersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SlideScreen) getActivity().findViewById(R.id.person_content)).setSlide(1);
            }
        });

        if(offersView.getVisibility() == View.GONE) {
            offersListHolder.setVisibility(View.GONE);
            RevealAnimation.expand(offersView, 500);
        }

        if(offers.size() < 1 && itsMe) {
            ((TextView) offersView.findViewById(R.id.offersText)).setText(R.string.offer_an_experience);
        }
        else {
            ((TextView) offersView.findViewById(R.id.offersText)).setText(
                    getResources().getQuantityString(R.plurals.offers, offers.size(), offers.size())
            );
        }

        if (offers.size() > 0 && offers.get(0).getPrice() < 0) {
            offersView.setBackgroundResource(R.color.purple);
        } else {
            offersView.setBackgroundResource(R.color.green);
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

                //TODO temp for delete arch (send my id list, server says which are gone)
                if(mPerson != null) {
                    team.realm.beginTransaction();

                    while(mPerson.getOffers().size() > 0) {
                        mPerson.getOffers().get(0).removeFromRealm();
                    }

                    team.realm.commitTransaction();
                }

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
                created.setText(TimeUtil.agoDate(mPerson.getCreated()));
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

            TextView actionButton = (TextView) personAbout.findViewById(R.id.action_button);

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
