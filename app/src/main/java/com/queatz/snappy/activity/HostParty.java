package com.queatz.snappy.activity;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.HostPartyAdapter;
import com.queatz.snappy.adapter.LocationAdapter;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.ui.TextView;
import com.queatz.snappy.ui.TimeSlider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by jacob on 1/3/15.
 */
public class HostParty extends BaseActivity {
    public Team team;
    private String mGroup;
    private Date mDate;
    private View mNewParty;
    private MapFragment mLocationMap;
    private com.queatz.snappy.things.Location mLocation;
    private ListView mSuggestedLocationsList;
    private Marker mMapMarker;
    private GoogleMap mGoogleMap;

    private Date percentToDate(float percent) {
        return Util.quantizeDate(new Date(mDate.getTime() + (int) (percent * Config.maxHoursInFuture * 1000 * 60 * 60)));
    }

    private float dateToPercent(Date date) {
        return (float) (date.getTime() - mDate.getTime()) / (float) (Config.maxHoursInFuture * 1000 * 60 * 60);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;
        mGroup = null;

        team.location.locate();

        setContentView(R.layout.host_party);

        final ListView partyList = ((ListView) findViewById(R.id.partyList));

        RealmResults<Party> recentParties = team.realm.where(Party.class)
                .equalTo("host.id", team.auth.getUser())
                .findAllSorted("date", false);
        partyList.setAdapter(new HostPartyAdapter(this, recentParties));

        /* New Party */

        mNewParty = View.inflate(this, R.layout.host_party_new, null);

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
                locationMapLayout.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            }
        });

        mDate = new Date();

        float p = .25f;

        if(Util.everybodyIsSleeping(percentToDate(p)))
            p = .75f;

        timeSlider.setPercent(p);

        timeSlider.setTextCallback(new TimeSlider.TextCallback() {
            @Override
            public String getText(float percent) {
                return Util.cuteDate(percentToDate(percent));
            }
        });

        searchLocation("");

        mSuggestedLocationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setLocation((com.queatz.snappy.things.Location) mSuggestedLocationsList.getAdapter().getItem(position));
            }
        });

        locationMapLayout.findViewById(R.id.locationMapMarker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGoogleMap == null)
                    return;

                com.queatz.snappy.things.Location location = new com.queatz.snappy.things.Location();

                final EditText locationName = (EditText) mNewParty.findViewById(R.id.location);

                location.setName(locationName.getText().toString());
                location.setLatitude(mGoogleMap.getCameraPosition().target.latitude);
                location.setLongitude(mGoogleMap.getCameraPosition().target.longitude);

                setLocation(location);
            }
        });

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

                team.action.hostParty(mGroup, name, date, mLocation, details);
                finish();
            }
        };

        mNewParty.findViewById(R.id.action_host).setOnClickListener(oclk);

        partyList.addHeaderView(mNewParty);
        partyList.addFooterView(new View(this));

        partyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setParty((Party) partyList.getAdapter().getItem(position));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void recenterMapWithInput(String q) {
        if(mLocationMap == null)
            return;

        team.location.getTopGoogleLocationForInput(q, new com.queatz.snappy.team.Location.AutocompleteCallback() {
            @Override
            public void onResult(final JSONObject result) {
                Log.d(Config.LOG_TAG, "result: " + result);

                mLocationMap.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        try {
                            JSONObject l = result.getJSONObject("geometry").getJSONObject("location");
                            LatLng latLng = new LatLng(l.getDouble("lat"), l.getDouble("lng"));
                            CameraUpdate center = CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, Config.defaultMapZoom));
                            googleMap.animateCamera(center);

                            if(mMapMarker == null) {
                                mMapMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
                            }
                            else {
                                mMapMarker.setPosition(latLng);
                            }

                            final String title = result.getString("name");
                            final String address = result.has("formatted_address") ? result.getString("formatted_address") : null;

                            mMapMarker.setTitle(title);
                            mMapMarker.showInfoWindow();

                            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                    com.queatz.snappy.things.Location location = new com.queatz.snappy.things.Location();

                                    final EditText locationName = (EditText) mNewParty.findViewById(R.id.location);

                                    location.setName(locationName.getText().toString());
                                    location.setLatitude(mMapMarker.getPosition().latitude);
                                    location.setLongitude(mMapMarker.getPosition().longitude);
                                    location.setAddress(address);

                                    setLocation(location);
                                }
                            });
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void lazyLoadMap() {
        if(mLocationMap != null)
            return;

        mLocationMap = new MapFragment();

        getFragmentManager().beginTransaction()
                .add(R.id.locationMapLayout, mLocationMap)
                .commit();

        mNewParty.post(new Runnable() {
            @Override
            public void run() {
                mNewParty.findViewById(R.id.locationMapMarker).bringToFront();
            }
        });

        mLocationMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;

                googleMap.setMyLocationEnabled(true);
                googleMap.setBuildingsEnabled(true);
                googleMap.setIndoorEnabled(true);

                Location l = team.location.get();

                if(l != null) {
                    LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());
                    CameraUpdate center = CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, Config.defaultMapZoom));
                    googleMap.moveCamera(center);
                }
            }
        });
    }

    public void searchLocation(String q) {
        if(q.isEmpty()) {
            mSuggestedLocationsList.setAdapter(null);
        }
        else {
            RealmResults<com.queatz.snappy.things.Location> results = team.realm.where(com.queatz.snappy.things.Location.class)
                    .beginsWith("name", q, RealmQuery.CASE_INSENSITIVE)
                    .findAllSorted("name", true);

            mSuggestedLocationsList.setAdapter(new LocationAdapter(this, results, 3));
        }

        final EditText locationAddress = (EditText) mNewParty.findViewById(R.id.locationAddress);
        locationAddress.setHint(String.format(getString(R.string.enter_address), q));
        locationAddress.setVisibility(q.trim().isEmpty() ? View.GONE : View.VISIBLE);

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

    public void setLocation(com.queatz.snappy.things.Location location) {
        EditText loc = ((EditText) mNewParty.findViewById(R.id.location));

        team.view.keyboard(loc, false);

        mLocation = location;
        mNewParty.findViewById(R.id.selectedLocation).setVisibility(View.VISIBLE);
        ((TextView) mNewParty.findViewById(R.id.selectedLocationName)).setText(mLocation.getName());

        mNewParty.findViewById(R.id.locationDetailsLayout).setVisibility(View.GONE);

        loc.setText(location.getName());

        EditText details = (EditText) mNewParty.findViewById(R.id.details);

        if(details.getText().toString().isEmpty()) {
            details.requestFocus();
            team.view.keyboard(details);
        }
    }

    public void setParty(Party party) {
        mGroup = party.getId();

        EditText name;

        name = ((EditText) mNewParty.findViewById(R.id.name));
        name.setText(party.getName());
        name.setEnabled(false);

        TimeSlider date = ((TimeSlider) mNewParty.findViewById(R.id.timeSlider));

        Date newDate = Util.matchDateHour(party.getDate());
        float percent = dateToPercent(newDate);

        Log.e(Config.LOG_TAG, newDate + " • " + percent);

        date.setPercent(percent);

        name = ((EditText) mNewParty.findViewById(R.id.details));
        name.setText(party.getDetails());

        setLocation(party.getLocation());

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
}

