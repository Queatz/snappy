package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.makeramen.RoundedImageView;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Camera;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.CircleTransform;
import com.queatz.snappy.ui.EditText;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.ui.camera.CameraFragment;
import com.queatz.snappy.ui.card.UpdateCard;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Date;
import java.util.List;

import io.realm.DynamicRealmObject;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by jacob on 8/7/16.
 */

public class MapSlide extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ViewGroup info;
    private Image image;

    Team team;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.map, container, false);

        team = ((MainApplication) getActivity().getApplication()).team;

        info = (ViewGroup) view.findViewById(R.id.info);

        final ImageButton cameraButton = (ImageButton) view.findViewById(R.id.cameraButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image == null) {
                    getPhoto();
                } else {
                    image = null;
                    Toast.makeText(getActivity(), getString(R.string.photo_removed), Toast.LENGTH_SHORT).show();
                    setImageActive();
                }
            }
        });

        final EditText whatsUp = (EditText) view.findViewById(R.id.whatsUp);

        whatsUp.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_GO == actionId) {
                    team.action.postSelfUpdate(Util.uriFromImage(image), whatsUp.getText().toString(), team.location.get());
                    whatsUp.setText("");
                }

                return false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        final Team team = ((MainApplication) getActivity().getApplication()).team;
        team.here.getRecentUpdates(getActivity());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(true);

        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showInfo((DynamicRealmObject) marker.getTag());
                return false;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                team.action.openLocation(getActivity(), (DynamicRealmObject) marker.getTag());
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showInfo(null);
            }
        });

        setMapPadding();

//        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//            @Override
//            public View getInfoWindow(Marker marker) {
//                TextView view = new TextView(MapSlide.this.getActivity());
//                view.setTypeface(null, Typeface.BOLD);
//                view.setTextColor(getResources().getColor(R.color.black));
//                view.setText(marker.getTitle());
//                return view;
//            }
//
//            @Override
//            public View getInfoContents(Marker marker) {
//                return null;
//            }
//        });

        Location location = team.location.get();

        if (location != null) {
            myLocationFound(location);
        } else {
            team.location.get(getActivity(), new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
                @Override
                public void onLocationFound(Location location) {
                    myLocationFound(location);
                }

                @Override
                public void onLocationUnavailable() {}
            });
        }

        setupMarkers();
    }

    private void getPhoto() {
        team.camera.getPhoto(getActivity(), new Camera.Callback() {
            @Override
            public void onPhoto(Image image) {
                MapSlide.this.image = image;
                setImageActive();
            }

            @Override
            public void onClosed() {
                // meep
            }
        });
    }

    private void showInfo(final DynamicRealmObject thing) {
        final Team team = ((MainApplication) getActivity().getApplication()).team;

        info.removeAllViews();

        if (thing == null) {
            info.setVisibility(View.GONE);
            setMapPadding();
            return;
        } else {
            info.setVisibility(View.VISIBLE);
        }

        if ("hub".equals(thing.getString(Thing.KIND))) {
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.hub_sheet, null);

            TextView details = (TextView) view.findViewById(R.id.details);
            details.setMovementMethod(new ScrollingMovementMethod());
            details.setText(thing.getString(Thing.ABOUT));

            ImageView photo = (ImageView) view.findViewById(R.id.profile);

            String photoUrl = Util.photoUrl(String.format(Config.PATH_EARTH_PHOTO, thing.getString(Thing.ID)), (int) Util.px(48));

            photo.setImageDrawable(null);
            photo.setVisibility(View.VISIBLE);

            Picasso.with(getActivity()).cancelRequest(photo);

            Picasso.with(getActivity())
                    .load(photoUrl)
                    .placeholder(R.color.spacer)
                    .into(photo);

            RealmChangeListener<DynamicRealmObject> changeListener = new RealmChangeListener<DynamicRealmObject>() {
                @Override
                public void onChange(DynamicRealmObject element) {
                    if (getActivity() == null) {
                        return;
                    }

                    List<DynamicRealmObject> contacts = thing.getList(Thing.CONTACTS);

                    LinearLayout contactsLayout = ((LinearLayout) view.findViewById(R.id.contacts));
                    View contactsHeader = view.findViewById(R.id.contactsHeader);

                    if(contacts.size() < 1) {
                        contactsLayout.setVisibility(View.GONE);
                        contactsHeader.setVisibility(View.GONE);
                    } else {
                        contactsLayout.setVisibility(View.VISIBLE);
                        contactsHeader.setVisibility(View.VISIBLE);

                        contactsLayout.removeAllViews();

                        for (DynamicRealmObject contact : contacts) {
                            final DynamicRealmObject member = contact.getObject(Thing.TARGET);
                            FrameLayout memberProfile = (FrameLayout) View.inflate(getActivity(), R.layout.contact, null);
                            contactsLayout.addView(memberProfile);
                            Picasso.with(getActivity())
                                    .load(member == null ? "" : Functions.getImageUrlForSize(contact.getObject(Thing.TARGET), (int) Util.px(64)))
                                    .placeholder(R.color.spacer)
                                    .into((RoundedImageView) memberProfile.findViewById(R.id.profile));

                            memberProfile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (getActivity() == null) {
                                        return;
                                    }

                                    if (member != null)
                                        team.action.openMessages(getActivity(), member);
                                }
                            });
                        }
                    }
                }
            };

            thing.addChangeListener(changeListener);
            changeListener.onChange(thing);

            team.api.get(Config.PATH_EARTH + "/" + thing.getString(Thing.ID), new Api.Callback() {
                @Override
                public void success(String response) {
                    team.things.put(response);
                }

                @Override
                public void fail(String response) {

                }
            });

            info.addView(view);

            setMapPadding();
        } else if ("update".equals(thing.getString(Thing.KIND))) {
            info.addView(new UpdateCard().getCard(getActivity(), thing, null, info, true));
        }
    }

    private void myLocationFound(Location location) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                location.getLatitude(),
                location.getLongitude()
        ), Config.defaultMapZoom));
    }

    private void setMapPadding() {
        if (mMap != null && getView() != null) {
            final View bottomLayout = getView().findViewById(R.id.bottomLayout);

            bottomLayout.post(new Runnable() {
                @Override
                public void run() {
                    mMap.setPadding(0, 0, 0, ((int) Util.px(-8)) + bottomLayout.getMeasuredHeight());
                }
            });
        }
    }

    RealmResults<DynamicRealmObject> things;

    private void setupMarkers() {
        Team team = ((MainApplication) getActivity().getApplication()).team;

        things = team.realm.where("Thing")
                .equalTo(Thing.KIND, "hub")
                .or()
                .beginGroup()
                    .equalTo(Thing.KIND, "update")
                    .greaterThanOrEqualTo(Thing.DATE, new Date(new Date().getTime() - 1000 * 60 * 60))
                    .isNotNull(Thing.LATITUDE)
                    .isNotNull(Thing.LONGITUDE)
                .endGroup()
                .findAll();

        things.addChangeListener(new RealmChangeListener<RealmResults<DynamicRealmObject>>() {
            @Override
            public void onChange(RealmResults<DynamicRealmObject> element) {
                updateMarkers();
            }
        });

        updateMarkers();
    }

    private void updateMarkers() {
        if (getActivity() == null) {
            return;
        }

        mMap.clear();

        for (DynamicRealmObject thing : things) {
            LatLng location = new LatLng(thing.getDouble(Thing.LATITUDE), thing.getDouble(Thing.LONGITUDE));

            MarkerOptions options = new MarkerOptions().position(location);

            if ("hub".equals(thing.getString(Thing.KIND))) {
                options.title(thing.getString(Thing.NAME));
            }

            final Marker marker = mMap.addMarker(options);

            marker.setTag(thing);

            // Placeholder
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(Util.tint(getResources().getColor(R.color.blue))));

            if ("hub".equals(thing.getString(Thing.KIND))) {
                Picasso.with(getActivity()).load(Util.photoUrl(String.format(Config.PATH_EARTH_PHOTO, thing.getString(Thing.ID)), (int) Util.px(64)))
                        .transform(new CircleTransform())
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

                                // Try / catch is because the marker may have been removed
                                try {
                                    marker.setIcon(icon);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
            } else if ("update".equals(thing.getString(Thing.KIND))) {
                String photo;

                if (thing.getBoolean(Thing.PHOTO)) {
                    photo = Util.photoUrl(String.format(Config.PATH_EARTH_PHOTO, thing.getString(Thing.ID)), (int) Util.px(32));
                } else {
                    photo = Functions.getImageUrlForSize(thing.getObject(Thing.PERSON), (int) Util.px(32));
                }

                Picasso.with(getActivity()).load(photo)
                        .transform(new CircleTransform())
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

                                // Try / catch is because the marker may have been removed
                                try {
                                    marker.setIcon(icon);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
            }
        }
    }

    public void setImageActive() {
        if (getView() == null) {
            return;
        }

        int color = R.color.gray;

        if (image != null) {
            color = R.color.blue;
        }

        ((ImageButton) getView().findViewById(R.id.cameraButton))
                .setImageTintList(ColorStateList.valueOf(getResources().getColor(color)));
    }
}