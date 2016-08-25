package com.queatz.snappy.fragment;

import android.app.Fragment;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.TextView;

/**
 * Created by jacob on 8/7/16.
 */

public class MapSlide extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
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
                return false;
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                TextView view = new TextView(MapSlide.this.getActivity());
                view.setTypeface(null, Typeface.BOLD);
                view.setTextColor(getResources().getColor(R.color.black));
                view.setText(marker.getTitle());
                return view;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        Team team = ((MainApplication) getActivity().getApplication()).team;

        Location location = team.location.get();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                location.getLatitude(),
                location.getLongitude()
        ), Config.defaultMapZoom));

//        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
//            @Override
//            public void onInfoWindowClose(Marker marker) {
//                marker.showInfoWindow();
//            }
//        });

//        ViewGroup root = (ViewGroup) findViewById(R.id.graphics);
//        custom = new ImageView(this);
//        custom.setImageResource(R.drawable.pickaxe);
//        root.addView(custom);

        // New bitmap descriptor
//        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.pickaxe);
//        BitmapDescriptor herbBitmap = BitmapDescriptorFactory.fromResource(R.drawable.herb);
//        BitmapDescriptor playerBitmap = BitmapDescriptorFactory.fromResource(R.drawable.player);

        // Add a marker in Sydney, Australia, and move the camera.
        // XXX TODO center on myLocation
//        LatLng fidi = new LatLng(37.7867653, -122.4060986);
//        marker = mMap.addMarker(new MarkerOptions().icon(playerBitmap).infoWindowAnchor(0.5f, 1 + (22f / 16f)).position(fidi).title("Jacob the Mage"));

//
////        for (int i = 0; i < 100; i++) {
////            LatLng latLng = new LatLng(fidi.latitude + (Math.random() - 0.5) * 0.1, fidi.longitude + (Math.random() - 0.5) * 0.1);
////            mMap.addMarker(new MarkerOptions().icon(herbBitmap).position(latLng).infoWindowAnchor(0.5f, 1 + (22f / 147f)).title("Dragon")).showInfoWindow();
////        }
//
//
//        for (int i = 0; i < 400; i++) {
//            LatLng latLng = new LatLng(fidi.latitude + (Math.random() - 0.5) * 0.1, fidi.longitude + (Math.random() - 0.5) * 0.1);
//            mMap.addMarker(new MarkerOptions().icon(playerBitmap).position(latLng).infoWindowAnchor(0.5f, 1 + (22f / 16f)).title("Player"));
//        }
    }
}