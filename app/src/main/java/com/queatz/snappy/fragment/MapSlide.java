package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.CircleTransform;
import com.queatz.snappy.ui.ContextualInputBar;
import com.queatz.snappy.ui.OnBackPressed;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Date;

import io.realm.DynamicRealmObject;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by jacob on 8/7/16.
 */

public abstract class MapSlide extends Fragment implements OnMapReadyCallback, OnBackPressed {

    private GoogleMap mMap;

    private DynamicRealmObject mMapFocus;
    Team team;

    // Injected by subclasses
    protected abstract ContextualInputBar getContextualInputBar();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    protected void initMap(View view) {
        team = ((MainApplication) getActivity().getApplication()).team;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        final Team team = ((MainApplication) getActivity().getApplication()).team;
        team.here.getRecentUpdates(getActivity());

        getContextualInputBar().addLayoutChangeListener(new Runnable() {
            @Override
            public void run() {
                setMapPadding();
            }
        });
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
            team.location.get(getActivity(), new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
                @Override
                public void onLocationFound(Location location) {
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLocationUnavailable() {

                }
            });
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                getContextualInputBar().showInfo((DynamicRealmObject) marker.getTag());
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
                getContextualInputBar().showInfo(null);
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

        if (mMapFocus != null) {
            setMapFocus(mMapFocus);
            getContextualInputBar().showInfo(mMapFocus);
        } else {
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
                    public void onLocationUnavailable() {
                    }
                });
            }
        }

        setupMarkers();
    }

    private void myLocationFound(Location location) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                location.getLatitude(),
                location.getLongitude()
        ), Config.defaultMapZoom));
    }

    private void setMapPadding() {
        if (mMap != null && getContextualInputBar() != null) {

            getContextualInputBar().post(new Runnable() {
                @Override
                public void run() {
                    mMap.setPadding(0, 0, 0, ((int) Util.px(-8)) + getContextualInputBar().getMeasuredHeight());
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
            final Marker marker = mMap.addMarker(options);

            marker.setTag(thing);

            // Placeholder
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(Util.tint(getResources().getColor(R.color.blue))));

            if ("hub".equals(thing.getString(Thing.KIND))) {
                Picasso.with(getActivity()).load(Util.photoUrl(String.format(Config.PATH_EARTH_PHOTO, thing.getString(Thing.ID)), (int) Util.px(48)))
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

    @Override
    public boolean onBackPressed() {
        if (getContextualInputBar().isInfoVisible()) {
             getContextualInputBar().showInfo(null);
            return true;
        } else {
            return false;
        }
    }

    public void setMapFocus(DynamicRealmObject mapFocus) {
        mMapFocus = null;

        if (mapFocus == null) {
            return;
        }

        if (mapFocus.isNull(Thing.LONGITUDE) || mapFocus.isNull(Thing.LATITUDE)) {
            return;
        }

        if (mMap != null) {
            double latitude = mapFocus.getDouble(Thing.LATITUDE);
            double longitude = mapFocus.getDouble(Thing.LONGITUDE);

            Location location = new Location("Village");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            myLocationFound(location);

            getContextualInputBar().showInfo(mapFocus);
        } else {
            mMapFocus = mapFocus;
        }
    }
}