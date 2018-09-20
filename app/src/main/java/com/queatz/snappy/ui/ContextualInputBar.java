package com.queatz.snappy.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.HostParty;
import com.queatz.snappy.adapter.OpenMessagesAction;
import com.queatz.snappy.adapter.PostSelfUpdateAction;
import com.queatz.snappy.adapter.SuggestionAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Camera;
import com.queatz.snappy.team.OnInfoChangedListener;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;
import com.queatz.snappy.team.actions.AddOfferAction;
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.team.actions.SigninAction;
import com.queatz.snappy.team.actions.UpdateThings;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.team.observers.AnonymousEnvironment;
import com.queatz.snappy.team.observers.AuthenticatedEnvironment;
import com.queatz.snappy.team.observers.CurrentEnvironment;
import com.queatz.snappy.team.observers.EnvironmentContext;
import com.queatz.snappy.team.observers.EnvironmentObserver;
import com.queatz.snappy.ui.card.UpdateCard;
import com.queatz.snappy.util.ContextualBehavior;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import io.realm.Case;
import io.realm.DynamicRealmObject;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 2/21/17.
 */

public class ContextualInputBar extends LinearLayout implements Branchable<ActivityContext>, EnvironmentContext {

    private EditText whatsUp;
    private ViewGroup info;
    private ImageView mProfile;
    private Uri image;
    private List<DynamicRealmObject> imWith = new ArrayList<>();
    private TextWatcher mTextWatcher;

    private DynamicRealmObject imAt;
    private boolean isGoing;

    private Runnable sendAction;

    private Team team;
    private EnvironmentObserver environmentObserver;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (environmentObserver == null) {
            environmentObserver = team.environment.observe(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (environmentObserver != null) {
            team.environment.forget(this);
            environmentObserver = null;
        }
    }

    @Override
    public void when(CurrentEnvironment change) {
        if (environmentObserver != null) {
            environmentObserver.when(change);
        }
    }

    @Override
    public void to(Branch<ActivityContext> branch) {
        Branch.from((ActivityContext) getContext()).to(branch);
    }

    private Collection<Runnable> resizeListeners = new HashSet<>();
    private ContextualBehavior lastBehavior;
    private OnInfoChangedListener infoChangedListener;

    public ContextualInputBar(Context context) {
        super(context);
        init();
    }

    public ContextualInputBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContextualInputBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        team = ((MainApplication) getContext().getApplicationContext()).team;
        environmentObserver = team.environment.observe(this);

        View.inflate(getContext(), R.layout.contextual_input_bar, this);

        final ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image == null) {
                    getPhoto();
                } else {
                    image = null;
                    Toast.makeText(getContext(), getContext().getString(R.string.photo_removed), Toast.LENGTH_SHORT).show();
                    updateImageButton();
                }
            }
        });

        whatsUp = (EditText) findViewById(R.id.whatsUp);

        findViewById(R.id.withLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imWith.clear();
                showImWith();
            }
        });

        whatsUp.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_GO == actionId) {
                    if (sendAction != null) {
                        sendAction.run();
                    }
                }

                return false;
            }
        });


        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendAction != null) {
                    sendAction.run();
                }
            }
        });

        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Need to get full length
                if (whatsUp.getText().length() < 1) {
                    showInfo(false);
                    return;
                }

                String possibleName = fetchPossibleName(s, start + count);

                if (possibleName == null) {
                    showImWith();
                    return;
                }

                suggest(possibleName);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        mProfile = (ImageView) findViewById(R.id.profile);

        when(new AuthenticatedEnvironment() {
            @Override
            public void then() {
                Images.with(team.context)
                        .load(Functions.getImageUrlForSize(team.auth.me(), (int) Util.px(64)))
                        .placeholder(R.color.spacer)
                        .into(mProfile);
            }
        });

        when(new AnonymousEnvironment() {
            @Override
            public void then() {
                mProfile.setImageResource(R.drawable.pickaxe);
            }
        });

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (team.environment.is(AuthenticatedEnvironment.class)) {
                    to(new OpenProfileAction(team.auth.me()));
                } else {
                    to(new SigninAction());
                }
            }
        });

        mProfile.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                team.view.show((Activity) getContext(), HostParty.class, null);
                return true;
            }
        });

        info = (ViewGroup) findViewById(R.id.info);
    }


    private void getPhoto() {
        team.camera.getPhoto((Activity) getContext(), new Camera.Callback() {
            @Override
            public void onPhoto(Uri uri) {
                image = uri;
                updateImageButton();
                whatsUp.requestFocus();

                whatsUp.post(new Runnable() {
                    @Override
                    public void run() {
                        team.view.keyboard(whatsUp);

                    }
                });
            }

            @Override
            public void onClosed() {
                // meep
            }
        });
    }

    public void checkIn(DynamicRealmObject thing, boolean isGoing) {
        imAt = thing;
        this.isGoing = isGoing;

        team.view.keyboard(whatsUp, true);
        whatsUp.requestFocus();

        updateAtIndicator();
    }


    public void postAsUpdate() {
        String text = whatsUp.getText().toString().trim();

        if (imAt == null && imWith.isEmpty() && image == null && text.isEmpty()) {
            return;
        }

        if (imAt != null) {
            imWith.add(imAt);
        }

        to(new PostSelfUpdateAction(image, text, team.location.get(), imWith, isGoing));
        resetAll();

        team.view.keyboard(whatsUp, false);
    }

    public void postAsWant() {
        String text = whatsUp.getText().toString().trim();

        if (text.isEmpty()) {
            return;
        }

        to(new AddOfferAction(text));

        // XXX todo only if add offer completely succeeded
        whatsUp.setText("");
        team.view.keyboard(whatsUp, false);
    }

    public void resetAll() {
        whatsUp.setText("");
        image = null;
        imAt = null;
        imWith.clear();
        updateImageButton();
        updateAtIndicator();
        showImWith();
        showInfo(false);
    }

    private String fetchPossibleName(CharSequence s, int caretPosition) {
        int startName = 0;

        for (int i = caretPosition - 1; i >= 0; i--) {
            if (!Character.isLetter(s.charAt(i))) {
                startName = i + 1;
                break;
            }
        }

        if (startName >= caretPosition) {
            return null;
        }

        return s.subSequence(startName, caretPosition).toString();
    }

    private void completeName(DynamicRealmObject person) {
        String name = person.getString(Thing.FIRST_NAME);

        EditText whatsUp = (EditText) findViewById(R.id.whatsUp);
        int caret = whatsUp.getSelectionStart();
        String possibleName = fetchPossibleName(whatsUp.getText(), caret);

        if (possibleName != null && name.length() > possibleName.length()) {
            whatsUp.getText().insert(caret, name.substring(possibleName.length()));
        }
    }

    private void suggest(String possibleName) {
        RealmQuery<DynamicRealmObject> query = team.realm.where("Thing")
                .equalTo(Thing.KIND, "person")
                .notEqualTo(Thing.ID, team.auth.getUser())
                .beginsWith(Thing.FIRST_NAME, possibleName, Case.SENSITIVE);

        for (DynamicRealmObject with : imWith) {
            query.notEqualTo(Thing.ID, with.getString(Thing.ID));
        }

        final RealmResults<DynamicRealmObject> suggestions = query.sort(Thing.INFO_DISTANCE, Sort.ASCENDING).findAll();

        if (suggestions.size() > 0) {
            showInfo(true);

            ListView personList = (ListView) LayoutInflater.from(getContext()).inflate(R.layout.suggestion_list, info, false);
            personList.setAdapter(new SuggestionAdapter(getContext(), suggestions));
            personList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    DynamicRealmObject person = suggestions.get(position);
                    imWith.add(person);

                    completeName(person);

                    showImWith();
                }
            });
            info.addView(personList);
        } else {
            showImWith();
        }
    }

    private void showImWith() {
        showInfo(false);

        LinearLayout withLayout = (LinearLayout) findViewById(R.id.withLayout);
        withLayout.removeAllViews();

        if (imWith.isEmpty()) {
            return;
        }

        int z = 0;
        for (DynamicRealmObject with : imWith) {
            View profile = LayoutInflater.from(getContext()).inflate(R.layout.with_person, withLayout, false);
            Images.with(getContext())
                    .load(Functions.getImageUrlForSize(with, (int) Util.px(48)))
                    .placeholder(R.color.spacer)
                    .into((ImageView) profile.findViewById(R.id.profile));

            withLayout.addView(profile, 0);
            profile.setZ(z++);
        }
    }


    private void updateAtIndicator() {
        ImageView at = (ImageView) findViewById(R.id.at);

        if (imAt != null) {
            mProfile.setVisibility(View.GONE);
            at.setVisibility(View.VISIBLE);
            Images.with(getContext()).load(Util.locationPhoto(imAt, (int) Util.px(48)))
                    .placeholder(R.drawable.location)
                    .into(at);

            at.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imAt = null;
                    updateAtIndicator();
                    Toast.makeText(getContext(), getContext().getString(R.string.location_removed), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mProfile.setVisibility(View.VISIBLE);
            at.setVisibility(View.GONE);
        }
    }

    private void updateImageButton() {
        int color = R.color.gray;

        if (image != null) {
            color = R.color.blue;
        }

        ((ImageButton) findViewById(R.id.cameraButton))
                .setImageTintList(ColorStateList.valueOf(getResources().getColor(color)));
    }

    public void showInfo(boolean show) {
        info.removeAllViews();

        if (!show) {
            info.setVisibility(View.GONE);
            layoutChange();
        } else {
            info.setVisibility(View.VISIBLE);

            if (getMeasuredWidth() > 0) {
                info.measure(
                        View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.UNSPECIFIED
                );
            }
        }

        if (infoChangedListener != null) {
            infoChangedListener.onInfoChanged(show);
        }
    }

    private void layoutChange() {
        for (Runnable runnable : resizeListeners) {
            runnable.run();
        }
    }

    public void addLayoutChangeListener(Runnable runnable) {
        resizeListeners.add(runnable);
    }

    public void removeLayoutChangeListener(Runnable runnable) {
        resizeListeners.remove(runnable);
    }

    public void showInfo(final DynamicRealmObject thing) {
        showInfo(thing != null);

        if (thing == null) {
            return;
        }

        if (ThingKinds.HUB.equals(thing.getString(Thing.KIND))) {
            final View view = LayoutInflater.from(getContext()).inflate(R.layout.hub_sheet, null);

            TextView details = (TextView) view.findViewById(R.id.details);
            details.setMovementMethod(new ScrollingMovementMethod());

            String about = thing.getString(Thing.ABOUT);

            if (about.isEmpty()) {
                details.setVisibility(View.GONE);
            } else {
                details.setVisibility(View.VISIBLE);
                details.setText(about);
            }
            ((TextView) view.findViewById(R.id.name)).setText(thing.getString(Thing.NAME));

            ImageView photo = (ImageView) view.findViewById(R.id.profile);

            String photoUrl = Util.photoUrl(String.format(Config.PATH_EARTH_PHOTO, thing.getString(Thing.ID)), (int) Util.px(48));

            photo.setImageDrawable(null);
            photo.setVisibility(View.VISIBLE);

            Images.with(getContext()).cancelRequest(photo);

            Images.with(getContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.location)
                    .into(photo);

            RealmChangeListener<DynamicRealmObject> changeListener = new RealmChangeListener<DynamicRealmObject>() {
                @Override
                public void onChange(DynamicRealmObject element) {
                    List<DynamicRealmObject> members = Util.membersOf(thing, ThingKinds.CONTACT);

                    LinearLayout contactsLayout = ((LinearLayout) view.findViewById(R.id.contacts));
                    View contactsHeader = view.findViewById(R.id.contactsHeader);

                    if (members.size() < 1) {
                        contactsLayout.setVisibility(View.GONE);
                        contactsHeader.setVisibility(View.GONE);
                    } else {
                        contactsLayout.setVisibility(View.VISIBLE);
                        contactsHeader.setVisibility(View.VISIBLE);

                        contactsLayout.removeAllViews();

                        for (DynamicRealmObject m : members) {
                            final DynamicRealmObject member = m.getObject(Thing.SOURCE).getObject(Thing.TARGET);

                            FrameLayout memberProfile = (FrameLayout) View.inflate(getContext(), R.layout.contact, null);
                            contactsLayout.addView(memberProfile);
                            Images.with(getContext())
                                    .load(member == null ? "" : Functions.getImageUrlForSize(member, (int) Util.px(64)))
                                    .placeholder(R.color.spacer)
                                    .into((RoundedImageView) memberProfile.findViewById(R.id.profile));

                            memberProfile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (member != null) {
                                        to(new OpenMessagesAction(member));
                                    }
                                }
                            });
                        }
                    }
                }
            };

            thing.addChangeListener(changeListener);
            changeListener.onChange(thing);

            team.earth.thing(thing.getString(Thing.ID), new Api.Callback() {
                @Override
                public void success(String response) {
                    to(new UpdateThings(response));
                }

                @Override
                public void fail(String response) {

                }
            });

            Location location = team.location.get();

            Button checkIn = (Button) view.findViewById(R.id.checkIn);
            if (location != null && Util.distance(location.getLatitude(), location.getLongitude(), thing.getDouble(Thing.LATITUDE), thing.getDouble(Thing.LONGITUDE)) < 0.189394 /* 1000ft */) {
                checkIn.setText(R.string.check_in);
                checkIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkIn(thing, false);
                        showInfo(false);
                    }
                });
            } else {
                checkIn.setText(R.string.going_here);
                checkIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkIn(thing, true);
                        showInfo(false);
                    }
                });
            }

            info.addView(view);

            layoutChange();
        } else if (ThingKinds.UPDATE.equals(thing.getString(Thing.KIND))) {
            info.addView(new UpdateCard().getCard(getContext(), thing, null, info, true));
        }

        info.post(new Runnable() {
            @Override
            public void run() {
                ((ScrollView) info).fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public boolean isInfoVisible() {
        return info.getVisibility() == View.VISIBLE;
    }

    public void switchBehavior(ContextualBehavior behavior) {
        if (lastBehavior != null) {
            lastBehavior.dispose(this);
        }

        lastBehavior = behavior;
        behavior.use(this);
    }

    public ContextualBehavior getCurrentBehavior() {
        return lastBehavior;
    }

    public void setHint(int hint) {
        whatsUp.setHint(hint);
    }

    public ContextualInputBar setSendAction(Runnable sendAction) {
        this.sendAction = sendAction;
        return this;
    }

    public String getText() {
        return whatsUp.getText().toString();
    }

    public void showCamera(boolean show) {
        findViewById(R.id.cameraButton).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void enableAutocomplete(boolean enable) {
        if (enable) {
            whatsUp.addTextChangedListener(mTextWatcher);
        } else {
            whatsUp.removeTextChangedListener(mTextWatcher);
        }
    }

    public void setInfoChangedListener(OnInfoChangedListener infoChangedListener) {
        this.infoChangedListener = infoChangedListener;
    }
}

