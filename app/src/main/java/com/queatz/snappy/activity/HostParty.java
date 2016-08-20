package com.queatz.snappy.activity;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.loopj.android.http.RequestParams;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.HostPartyAdapter;
import com.queatz.snappy.adapter.LocationAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.ui.TimeSlider;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.util.Date;

import io.realm.Case;
import io.realm.DynamicRealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jacob on 1/3/15.
 */
public class HostParty extends Activity {
    public Team team;
    private String mGroup;
    private Date mDate;
    private View mNewParty;
    private MapView mLocationMap;
    private DynamicRealmObject mLocation;
    private ListView mSuggestedLocationsList;
    private Marker mMapMarker;
    private GoogleMap mGoogleMap;

    private Date percentToDate(float percent) {
        return TimeUtil.quantizeDate(new Date(mDate.getTime() + (int) (percent * Config.maxHoursInFuture * 1000 * 60 * 60)));
    }

    private float dateToPercent(Date date) {
        return (float) (date.getTime() - mDate.getTime()) / (float) (Config.maxHoursInFuture * 1000 * 60 * 60);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mLocationMap != null) {
            mLocationMap.onCreate(savedInstanceState);
        }

        team = ((MainApplication) getApplication()).team;
        mGroup = null;

        team.location.locate(this);

        setContentView(R.layout.host_party);

        final ListView partyList = ((ListView) findViewById(R.id.partyList));

        /* New Party */

        View.OnClickListener oclk = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Team team = ((MainApplication) getApplication()).team;

                String name = ((EditText) mNewParty.findViewById(R.id.name)).getText().toString();
                Date date = percentToDate(((TimeSlider) mNewParty.findViewById(R.id.timeSlider)).getPercent());
                String details = ((EditText) mNewParty.findViewById(R.id.details)).getText().toString();

                if(name.isEmpty()) {
                    Toast.makeText(HostParty.this, ((EditText) mNewParty.findViewById(R.id.name)).getHint().toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mLocation == null) {
                    Toast.makeText(HostParty.this, getString(R.string.enter_location), Toast.LENGTH_SHORT).show();
                    return;
                }

                team.action.hostParty(HostParty.this, mGroup, name, date, mLocation, details);
                finish();
            }
        };

        mNewParty = View.inflate(this, R.layout.host_party_new, null);

        mNewParty.findViewById(R.id.action_host).setOnClickListener(oclk);

        partyList.addHeaderView(mNewParty);
        partyList.addFooterView(new View(this));

        RealmResults<DynamicRealmObject> recentParties = team.realm.where("Thing")
                .equalTo(Thing.KIND, "party")
                .equalTo("host.id", team.auth.getUser())
                .findAllSorted("date", Sort.DESCENDING);
        partyList.setAdapter(new HostPartyAdapter(this, recentParties));

        TimeSlider timeSlider = (TimeSlider) mNewParty.findViewById(R.id.timeSlider);

        final View selectedLocation = mNewParty.findViewById(R.id.selectedLocation);
        final EditText location = (EditText) mNewParty.findViewById(R.id.location);
        final EditText locationAddress = (EditText) mNewParty.findViewById(R.id.locationAddress);
        mSuggestedLocationsList = (ListView) mNewParty.findViewById(R.id.suggestedLocations);
        final View locationMapLayout = mNewParty.findViewById(R.id.locationMapLayout);

        TextView.OnEditorActionListener searchableBlank = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    team.view.keyboard(v, false);
                    return true;
                }

                return false;
            }
        };

        TextView.OnEditorActionListener searchable = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    team.view.keyboard(v, false);
                    recenterMapWithInput(v.getText().toString());
                    return true;
                }

                return false;
            }
        };

        location.setOnEditorActionListener(searchableBlank);
        locationAddress.setOnEditorActionListener(searchable);

        selectedLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLocation();
            }
        });

        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchLocation(location.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        locationAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                lazyLoadMap();

                if(locationMapLayout.getVisibility() == View.GONE && hasFocus)
                    locationMapLayout.setVisibility(View.VISIBLE);
            }
        });

        mDate = new Date();

        float p = .25f;

        if(TimeUtil.everybodyIsSleeping(percentToDate(p)))
            p = .75f;

        timeSlider.setPercent(p);

        timeSlider.setTextCallback(new TimeSlider.TextCallback() {
            @Override
            public String getText(float percent) {
                return TimeUtil.cuteDate(percentToDate(percent));
            }
        });

        searchLocation("");

        mSuggestedLocationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setLocation((DynamicRealmObject) mSuggestedLocationsList.getAdapter().getItem(position));
            }
        });

        locationMapLayout.findViewById(R.id.locationMapMarker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGoogleMap == null)
                    return;

                final EditText locationName = (EditText) mNewParty.findViewById(R.id.location);

                DynamicRealmObject location = team.realm.createObject("Thing");
                location.setString(Thing.KIND, "location");
                location.setString(Thing.NAME, locationName.getText().toString());
                location.setDouble(Thing.LATITUDE, mGoogleMap.getCameraPosition().target.latitude);
                location.setDouble(Thing.LONGITUDE, mGoogleMap.getCameraPosition().target.longitude);

                setLocation(location);
            }
        });

        partyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setParty((DynamicRealmObject) partyList.getItemAtPosition(position));
            }
        });

        fetchParties();
    }

    @Override
    public void onDestroy() {
        if(mLocationMap != null) {
            mLocationMap.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mLocationMap != null) {
            mLocationMap.onResume();
        }
    }

    @Override
    public void onPause() {
        if(mLocationMap != null) {
            mLocationMap.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mLocationMap != null) {
            mLocationMap.onSaveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        if(mLocationMap != null) {
            mLocationMap.onLowMemory();
        }

        super.onLowMemory();
    }

    private void recenterMapWithInput(String q) {
        if(mLocationMap == null)
            return;

        team.location.getTopGoogleLocationForInput(q, new com.queatz.snappy.team.Location.AutocompleteCallback() {
            @Override
            public void onResult(final JsonObject result) {
                Log.d(Config.LOG_TAG, "result: " + result);

                mLocationMap.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        JsonObject l = result.getAsJsonObject("geometry").getAsJsonObject("location");
                        LatLng latLng = new LatLng(l.get("lat").getAsDouble(), l.get("lng").getAsDouble());
                        CameraUpdate center = CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, Config.defaultMapZoom));
                        googleMap.animateCamera(center);

                        if (mMapMarker == null) {
                            mMapMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
                        } else {
                            mMapMarker.setPosition(latLng);
                        }

                        final String title = result.get("name").getAsString();
                        final String address = result.has("formatted_address") ? result.get("formatted_address").getAsString() : null;

                        mMapMarker.setTitle(title);
                        mMapMarker.showInfoWindow();

                        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                final EditText locationName = (EditText) mNewParty.findViewById(R.id.location);

                                DynamicRealmObject location = team.realm.createObject("Thing");
                                location.setString(Thing.KIND, "location");
                                location.setString(Thing.NAME, locationName.getText().toString());
                                location.setDouble(Thing.LATITUDE, mMapMarker.getPosition().latitude);
                                location.setDouble(Thing.LONGITUDE, mMapMarker.getPosition().longitude);
                                location.setString(Thing.ADDRESS, address);

                                setLocation(location);
                            }
                        });
                    }
                });
            }
        });
    }

    private void lazyLoadMap() {
        if(mLocationMap != null)
            return;

        ViewGroup locationMapLayout = (ViewGroup) mNewParty.findViewById(R.id.locationMapLayout);
        MapsInitializer.initialize(this);

        GoogleMapOptions googleMapOptions = new GoogleMapOptions();
        googleMapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL);

        Location l = team.location.get();

        if(l != null) {
            LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());

            googleMapOptions.camera(new CameraPosition(latLng, Config.defaultMapZoom, 0, 0));
        }

        mLocationMap = new MapView(this, googleMapOptions);
        mLocationMap.onCreate(null);
        mLocationMap.onResume();
        locationMapLayout.addView(mLocationMap, 0);

        mLocationMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;

                googleMap.setMyLocationEnabled(true);
                googleMap.setBuildingsEnabled(true);
                googleMap.setIndoorEnabled(true);

                Location l = team.location.get();

                if (l != null) {
                    LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());
                    CameraUpdate center = CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, Config.defaultMapZoom));
                    googleMap.moveCamera(center);
                }
            }
        });
    }

    private void fetchParties() {
        team.api.get(Config.PATH_EARTH + "/" + team.auth.getUser() + Config.PATH_PARTIES, null, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.putAll(response);
            }

            @Override
            public void fail(String response) {

            }
        });
    }

    public void searchLocation(String q) {
        updateLocationSuggestions(q);
        fetchLocations(q);

        final EditText locationAddress = (EditText) mNewParty.findViewById(R.id.locationAddress);
        locationAddress.setHint(String.format(getString(R.string.enter_address), q));
        locationAddress.setVisibility(q.trim().isEmpty() ? View.GONE : View.VISIBLE);
        mNewParty.findViewById(R.id.locationMapLayout).setVisibility(View.GONE);

    }

    public void updateLocationSuggestions(String q) {
        if(q.isEmpty()) {
            mSuggestedLocationsList.setAdapter(null);
            return;
        }

        RealmResults<DynamicRealmObject> results = team.realm.where("Thing")
                .equalTo(Thing.KIND, "hub")
                .beginsWith("name", q, Case.INSENSITIVE)
                .findAllSorted("name", Sort.ASCENDING);

        mSuggestedLocationsList.setAdapter(new LocationAdapter(this, results, 3));
    }

    public void fetchLocations(String q) {
        Location location = team.location.get();

        if(location == null)
            return;

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LATITUDE, location.getLatitude());
        params.put(Config.PARAM_LONGITUDE, location.getLongitude());
        params.put(Config.PARAM_NAME, q);

        team.api.get(Config.PATH_EARTH + "/" + Config.PATH_LOCATIONS, params, new Api.Callback() {
            @Override
            public void success(String response) {
                team.things.putAll(response);

                EditText location = ((EditText) mNewParty.findViewById(R.id.location));

                if (location != null) {
                    String q = location.getText().toString();
                    updateLocationSuggestions(q);
                }
            }

            @Override
            public void fail(String response) {
                Log.w(Config.LOG_TAG, "Couldn't fetch locations for \"" + response + "\"");
            }
        });
    }

    public void editLocation() {
        mLocation = null;
        mNewParty.findViewById(R.id.selectedLocation).setVisibility(View.GONE);
        mNewParty.findViewById(R.id.locationDetailsLayout).setVisibility(View.VISIBLE);

        ((EditText) mNewParty.findViewById(R.id.locationAddress)).setText("");

        mNewParty.findViewById(R.id.location).requestFocus();
        team.view.keyboard((EditText) mNewParty.findViewById(R.id.location));
        ((EditText) mNewParty.findViewById(R.id.location)).selectAll();
    }

    public void setLocation(DynamicRealmObject location) {
        EditText loc = ((EditText) mNewParty.findViewById(R.id.location));

        team.view.keyboard(loc, false);

        mLocation = location;
        mNewParty.findViewById(R.id.selectedLocation).setVisibility(View.VISIBLE);
        ((TextView) mNewParty.findViewById(R.id.selectedLocationName)).setText(mLocation.getString(Thing.NAME));

        ImageView locationProfile = ((ImageView) mNewParty.findViewById(R.id.selectedLocationProfile));

        int s = (int) Util.px(128);
        String photoUrl = Config.API_URL + String.format(Config.PATH_LOCATION_PHOTO + "?s=" + s + "&auth=" + team.auth.getAuthParam(), location.getString(Thing.ID));

        Picasso.with(team.context).load(photoUrl).placeholder(R.drawable.location).into(locationProfile);

        mNewParty.findViewById(R.id.locationDetailsLayout).setVisibility(View.GONE);

        loc.setText(location.getString(Thing.NAME));

        EditText details = (EditText) mNewParty.findViewById(R.id.details);

        if(details.getText().toString().isEmpty()) {
            details.requestFocus();
            team.view.keyboard(details);
        }

        setMapLocation(location);
    }

    protected void setMapLocation(DynamicRealmObject l) {
        if(mGoogleMap == null || l == null)
            return;

        LatLng latLng = new LatLng(l.getDouble(Thing.LATITUDE), l.getDouble(Thing.LONGITUDE));
        CameraUpdate center = CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, Config.defaultMapZoom));
        mGoogleMap.animateCamera(center);
    }

    public void setParty(DynamicRealmObject party) {
        mGroup = party.getString(Thing.ID);

        EditText name;

        name = ((EditText) mNewParty.findViewById(R.id.name));
        name.setText(party.getString(Thing.NAME));
        name.setEnabled(false);

        TimeSlider date = ((TimeSlider) mNewParty.findViewById(R.id.timeSlider));

        Date newDate = TimeUtil.matchDateHour(party.getDate(Thing.DATE));
        float percent = dateToPercent(newDate);

        Log.e(Config.LOG_TAG, newDate + " • " + percent);

        date.setPercent(percent);

        name = ((EditText) mNewParty.findViewById(R.id.details));
        name.setText(party.getString(Thing.ABOUT));

        setLocation(party.getObject(Thing.LOCATION));

        ((ListView) findViewById(R.id.partyList)).smoothScrollToPosition(0);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getMenuInflater();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        team.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

