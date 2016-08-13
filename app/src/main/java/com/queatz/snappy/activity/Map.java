package com.queatz.snappy.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.queatz.snappy.R;
import com.queatz.snappy.ui.TextView;

/**
 * Created by jacob on 8/7/16.
 */

public class Map extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    private GoogleMap mMap;
//    private ImageView custom;
    private Marker marker;
    private LatLng targetPosition;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void gameLoop() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePositions();
                gameLoop();
            }
        }, 10);
    }

    private void updateGraphics() {
//        Point point = mMap.getProjection().toScreenLocation(new LatLng(37.7867653, -122.4060986));
////        mMap.getCameraPosition().zoom; || tilt
//
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) custom.getLayoutParams();
//        params.topMargin = point.y - custom.getMeasuredHeight();
//        params.leftMargin = point.x - custom.getMeasuredHeight() / 2;
//        custom.setLayoutParams(params);
    }

    private void updatePositions() {
        if (targetPosition == null) {
            return;
        }

        marker.setPosition(new LatLng(
                marker.getPosition().latitude + (targetPosition.latitude - marker.getPosition().latitude) / 10,
                marker.getPosition().longitude + (targetPosition.longitude - marker.getPosition().longitude) / 10
        ));
    }

    @Override
    public void onCameraMove() {
        updateGraphics();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setMinZoomPreference(16);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng position = marker.getPosition();
                targetPosition = new LatLng(position.latitude + (
                        Math.random() - 0.5) * .001,
                        position.longitude + (Math.random() - 0.5) * .001
                );
                return false;
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                TextView view = new TextView(Map.this);
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
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.pickaxe);
        BitmapDescriptor herbBitmap = BitmapDescriptorFactory.fromResource(R.drawable.herb);
        BitmapDescriptor playerBitmap = BitmapDescriptorFactory.fromResource(R.drawable.player);

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng fidi = new LatLng(37.7867653, -122.4060986);
        marker = mMap.addMarker(new MarkerOptions().icon(playerBitmap).infoWindowAnchor(0.5f, 1 + (22f / 16f)).position(fidi).title("Jacob the Mage"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fidi, 19.37f));


        for (int i = 0; i < 100; i++) {
            LatLng latLng = new LatLng(fidi.latitude + (Math.random() - 0.5) * 0.1, fidi.longitude + (Math.random() - 0.5) * 0.1);
            mMap.addMarker(new MarkerOptions().icon(herbBitmap).position(latLng).infoWindowAnchor(0.5f, 1 + (22f / 147f)).title("Dragon")).showInfoWindow();
        }


        for (int i = 0; i < 400; i++) {
            LatLng latLng = new LatLng(fidi.latitude + (Math.random() - 0.5) * 0.1, fidi.longitude + (Math.random() - 0.5) * 0.1);
            mMap.addMarker(new MarkerOptions().icon(playerBitmap).position(latLng).infoWindowAnchor(0.5f, 1 + (22f / 16f)).title("Player"));
        }

        gameLoop();
    }
}