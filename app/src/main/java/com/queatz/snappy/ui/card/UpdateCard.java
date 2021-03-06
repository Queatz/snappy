package com.queatz.snappy.ui.card;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.queatz.branch.Branch;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.activity.Main;
import com.queatz.snappy.adapter.CommentAdapter;
import com.queatz.snappy.adapter.PostCommentOnAction;
import com.queatz.snappy.adapter.ShowLikersAction;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;
import com.queatz.snappy.team.actions.LikeUpdateAction;
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.team.actions.ShareThingAction;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.team.observers.AuthenticatedEnvironment;
import com.queatz.snappy.ui.ZoomableImageView;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;
import com.queatz.snappy.util.TimeUtil;

import java.util.List;

import io.realm.DynamicRealmObject;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 8/22/16.
 */
public class UpdateCard implements Card<DynamicRealmObject> {
    @Override
    public View getCard(final Context context, final DynamicRealmObject update, View convertView, ViewGroup parent) {
        return getCard(context, update, convertView, parent, false);
    }

    public View getCard(final Context context, final DynamicRealmObject update, View convertView, ViewGroup parent, boolean isFloating) {
        final Branch<ActivityContext> branch = Branch.from((ActivityContext) context);
        final View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.person_upto_item, parent, false);
        }

        if (isFloating) {
            view.setBackgroundResource(R.drawable.white_rounded);
            view.findViewById(R.id.highlight).setBackgroundResource(R.drawable.blue_rounded_top);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setElevation(0);
            }
        } else {
            view.setBackgroundResource(R.color.white);
            view.findViewById(R.id.highlight).setBackgroundResource(R.color.blue);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setElevation(Util.px(2));
            }
        }

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        final DynamicRealmObject person = update.getObject(Thing.SOURCE);
        final DynamicRealmObject location = !update.isNull(Thing.TARGET) && !update.getObject(Thing.TARGET).isNull(Thing.LOCATION) ?
                update.getObject(Thing.TARGET).getObject(Thing.LOCATION) : null;

        if(person != null) {
            int s = (int) Util.px(64);

            ImageView profile = (ImageView) view.findViewById(R.id.profile);

            Images.with(context)
                    .load(location == null ? Functions.getImageUrlForSize(person, s) : Util.locationPhoto(location, s))
                    .placeholder(location == null ? R.color.spacer : R.drawable.location)
                    .into(profile);

            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    branch.to(new OpenProfileAction(person));
                }
            });

            TextView type = (TextView) view.findViewById(R.id.type);

            if (Config.UPDATE_ACTION_UPTO.equals(update.getString(Thing.ACTION))) {
                type.setText(context.getString(R.string.person_posted, person.getString(Thing.FIRST_NAME)));
            } else {
                type.setText(person.getString(Thing.FIRST_NAME));
            }

            TextView time = (TextView) view.findViewById(R.id.time);
            time.setText(TimeUtil.agoDate(update.getDate(Thing.DATE)));
        }

        view.setClickable(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(team.context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    branch.to(new LikeUpdateAction(update));
                    updateLikes(view, update, context);
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        updateLikes(view, update, context);

        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (Config.UPDATE_ACTION_UPTO.equals(update.getString(Thing.ACTION))) {
            if (update.getBoolean(Thing.PHOTO)) {
                photo.setVisibility(View.VISIBLE);

                Util.setPhotoWithPicasso(update, parent.getMeasuredWidth(), photo);
                ZoomableImageView.zoomable(photo);
            } else {
                photo.setVisibility(View.GONE);
            }

            if(update.isNull(Thing.ABOUT) || update.getString(Thing.ABOUT).isEmpty()) {
                view.findViewById(R.id.details).setVisibility(View.GONE);
            }
            else {
                view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            }
        }
        else {
            view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            view.findViewById(R.id.likers).setVisibility(View.GONE);
            photo.setVisibility(View.GONE);
        }

        if (!update.isNull(Thing.JOINS)) {
            List<DynamicRealmObject> withThings = team.realm.where("Thing")
                    .equalTo(Thing.KIND, ThingKinds.JOIN)
                    .equalTo("target.id", update.getString(Thing.ID))
                    .findAll();
            List<DynamicRealmObject> people = new RealmList<>();
            List<DynamicRealmObject> hubs = new RealmList<>();

            for (DynamicRealmObject withThing : withThings) {
                if (ThingKinds.PERSON.equals(withThing.getObject(Thing.SOURCE).getString(Thing.KIND))) {
                    people.add(withThing);
                } else if (ThingKinds.HUB.equals(withThing.getObject(Thing.SOURCE).getString(Thing.KIND))) {
                    hubs.add(withThing);
                }
            }

            TextView withAt = (TextView) view.findViewById(R.id.withAt);
            if (people.size() > 0 || hubs.size() > 0) {
                withAt.setVisibility(View.VISIBLE);

                SpannableStringBuilder builder = new SpannableStringBuilder();

                if (people.size() > 0) {
                    builder.append("with");

                    int added = 0;
                    for (final DynamicRealmObject with : people) {
                        String prefix = (added > 0 ? (added == people.size() - 1 ? " and " : ", ") : " ");
                        SpannableString ss = new SpannableString(prefix + Functions.getFullName(with.getObject(Thing.SOURCE)));

                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                branch.to(new OpenProfileAction(with.getObject(Thing.SOURCE)));
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        ss.setSpan(new AbsoluteSizeSpan(16, true), prefix.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ss.setSpan(clickableSpan, prefix.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        builder.append(ss);
                        added++;
                    }
                }

                if (hubs.size() > 0) {
                    if (people.size() > 0) {
                        builder.append(" ");
                    }

                    boolean isGoing = update.getBoolean(Thing.GOING);

                    builder.append(isGoing ? "going to" : "at");

                    for (final DynamicRealmObject with : hubs) {
                        SpannableString ss = new SpannableString(" " + with.getObject(Thing.SOURCE).getString(Thing.NAME));

                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                // XXX Show on map
                                Bundle extras = new Bundle();
                                extras.putString(Config.EXTRA_SHOW, Main.MAP_SLIDE);
                                extras.putString(Config.EXTRA_MAP_FOCUS_ID, with.getObject(Thing.SOURCE).getString(Thing.ID));
                                team.view.show((Activity) context, Main.class, extras);
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        ss.setSpan(new AbsoluteSizeSpan(16, true), 1, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ss.setSpan(clickableSpan, 1, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        builder.append(ss);
                    }

                    withAt.setMovementMethod(LinkMovementMethod.getInstance());
                    withAt.setText(builder);
                }

                withAt.setMovementMethod(LinkMovementMethod.getInstance());
                withAt.setText(builder);
            } else {
                withAt.setVisibility(View.GONE);
            }
        }

        ((TextView) view.findViewById(R.id.details)).setText(
                Util.getUpdateText(update)
        );

        Button shareButton = (Button) view.findViewById(R.id.shareButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareButton.getCompoundDrawables()[0].setTint(context.getResources().getColor(R.color.gray));
        }

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                branch.to(new ShareThingAction(update));
            }
        });

        RealmResults<DynamicRealmObject> with = team.realm.where("Thing")
                .equalTo(Thing.KIND, ThingKinds.MEMBER)
                .equalTo(Thing.SOURCE + "." + Thing.KIND, ThingKinds.UPDATE)
                .equalTo(Thing.TARGET + "." + Thing.ID, update.getString(Thing.ID))
                .findAll()
                .sort(Thing.DATE, Sort.ASCENDING);
        CommentAdapter commentsAdapter = new CommentAdapter(context, with);

        ((ListView) view.findViewById(R.id.commentsList)).setAdapter(commentsAdapter);

        final EditText writeComment = (EditText) view.findViewById(R.id.writeComment);

        view.findViewById(R.id.sendCommentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                branch.to(new PostCommentOnAction(update, writeComment.getText().toString()));
                clearComment(team, writeComment);
            }
        });

        writeComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_GO == actionId) {
                    branch.to(new PostCommentOnAction(update, writeComment.getText().toString()));
                    clearComment(team, writeComment);
                }

                return false;
            }
        });

        writeComment.setText("");

        return view;
    }

    private void clearComment(Team team, EditText comment) {
        comment.setText("");
        team.view.keyboard(comment, false);
    }

    private void updateLikes(final View view, final DynamicRealmObject update, final Context context) {
        final Team team = ((MainApplication) context.getApplicationContext()).team;
        final Branch<ActivityContext> branch = Branch.from((ActivityContext) context);


        final Button likers = (Button) view.findViewById(R.id.likers);

        int likersCount = (int) team.realm.where("Thing")
                .equalTo(Thing.KIND, "like")
                .equalTo("target.id", update.getString(Thing.ID))
                .count();

        if (update.getInt(Thing.LIKERS) > likersCount) {
            likersCount = update.getInt(Thing.LIKERS);
        }

        boolean byMe = team.environment.is(AuthenticatedEnvironment.class) && Util.liked(update, team.auth.me());

        likers.setCompoundDrawablesWithIntrinsicBounds(byMe ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp, 0, 0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            likers.getCompoundDrawables()[0].setTint(context.getResources().getColor(R.color.red));
        }

        likers.setVisibility(View.VISIBLE);

        if (likersCount > 0) {
            likers.setText(team.context.getResources().getQuantityString(R.plurals.likes, likersCount, likersCount));
            likers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    branch.to(new ShowLikersAction(update));
                }
            });
        } else {
            likers.setText("");
            likers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Branch.from((ActivityContext) context).to(new LikeUpdateAction(update));
                    updateLikes(view, update, context);
                }
            });
        }

//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) likers.getLayoutParams();
//
//        if (params == null) {
//            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        }
//
//        if (View.GONE == view.findViewById(R.id.details).getVisibility()) {
//            params.bottomMargin = 0;
//        } else {
//            params.bottomMargin = (int) Util.px(-8);
//        }

//        likers.setLayoutParams(params);
    }
}
