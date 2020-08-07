package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.sql.Timestamp;
import java.util.Date;

public class EventForm extends AppCompatActivity
        implements OnMapReadyCallback {

    MaterialEditText eventName;
    MaterialEditText location;
    MaterialEditText description;
    MaterialAutoCompleteTextView myd;
    MaterialAutoCompleteTextView hourAndMinutes;
    Button submit_btn;
    TextView title;
    DatabaseReference reference;
    DatabaseReference userreference;
    DatabaseReference eventinforeference;
    Context context;

    // latitude and longitude
    String latitude;
    String longitude;

    GoogleMap mMap;
    Location myLocation;
    boolean mLocationPermissionGranted = false;
    private static final int MY_PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        // Google map SDK
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.event_form_map);
        mapFragment.getMapAsync(this);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String userID = currentUser.getUid();
        eventName = findViewById(R.id.eventformname);
        location = findViewById(R.id.eventformlocation);
        description = findViewById(R.id.eventformdesc);
        myd = findViewById(R.id.eventformdate);
        hourAndMinutes = findViewById(R.id.eventformtime);
        submit_btn = findViewById(R.id.formsubmit);
        title = findViewById(R.id.eventformtitle);
        context = this;

        Intent intent = getIntent();
        final String groupName = intent.getStringExtra("groupName");
        final String timestamp = intent.getStringExtra("ts");
        if (!timestamp.isEmpty()) {
            reference = FirebaseDatabase.getInstance().getReference("Groups")
                    .child(groupName).child("Events").child(timestamp).child("EventInfo");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String eventname = snapshot.child("EventName").getValue().toString();
                    String loc = snapshot.child("Location").getValue().toString();
                    String time = snapshot.child("EventTime").getValue().toString();
                    String desc = snapshot.child("Description").getValue().toString();
                    String eventdate = time.split(", ")[0];
                    String eventtime = time.split(", ")[1];
                    title.setText("Modify Event");
                    eventName.setText(eventname);
                    location.setText(loc);
                    myd.setText(eventdate);
                    hourAndMinutes.setText(eventtime);
                    description.setText(desc);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupName).child("Events");
        userreference = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("Groups").child(groupName);
        final Calendar myCalendar = Calendar.getInstance();

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = eventName.getText().toString();
                String loc = location.getText().toString();
                String desc = description.getText().toString();
                String ddate = myd.getText().toString();
                String hm = hourAndMinutes.getText().toString();
                String time = ddate + ", " + hm;

                if (name.isEmpty()) {
                    Toast.makeText(EventForm.this, "Event name is required!", Toast.LENGTH_SHORT).show();
                } else if (loc.isEmpty()) {
                    Toast.makeText(EventForm.this, "Location is required!", Toast.LENGTH_SHORT).show();
                } else if (desc.isEmpty()) {
                    Toast.makeText(EventForm.this, "Description is required!", Toast.LENGTH_SHORT).show();
                } else if (ddate.isEmpty()) {
                    Toast.makeText(EventForm.this, "Date is required!", Toast.LENGTH_SHORT).show();
                } else if (hm.isEmpty()) {
                    Toast.makeText(EventForm.this, "Time is required!", Toast.LENGTH_SHORT).show();
                } else if (latitude == null || longitude == null) {
                    Toast.makeText(EventForm.this, "Long click to drag the map maker to the location!", Toast.LENGTH_SHORT).show();
                } else {
                    final HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("EventName", name);
                    hashMap.put("Admin", userID);
                    hashMap.put("Description", desc);
                    hashMap.put("Location", loc);
                    hashMap.put("EventTime", time);
                    hashMap.put("Latitude", latitude);
                    hashMap.put("Longitude", longitude);
                    Date dt = new Date();
                    long t = dt.getTime();
                    final String ts = timestamp.isEmpty() ? new Timestamp(t).toString().replace("-", "").
                            replace(" ", "").
                            replace(":", "").
                            replace(".", "") : timestamp;
                    hashMap.put("EventToken", ts);

                    HashMap<String, String> users = new HashMap<>();
                    users.put(userID, "1");
                    reference.child(ts).child("EventUsers").setValue(users, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(context, "Event creation failed. Code 1", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("hashmappp", "" + hashMap.size());
                                eventinforeference = FirebaseDatabase.getInstance().getReference("Groups").child(groupName).child("Events");
                                eventinforeference.child(ts).child("EventInfo").setValue(hashMap, new DatabaseReference.CompletionListener() {

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error != null) {
                                            Toast.makeText(context, "Event creation failed. Code 2", Toast.LENGTH_SHORT).show();
                                        } else {
                                            userreference.child(ts).child("EventInfo").setValue(hashMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                    //if is from Groupinfoactivity then back to the event just created
                                                    if (timestamp.isEmpty()) {
                                                        Intent intent1 = new Intent(context, EventActivity.class);
                                                        intent1.putExtra("token", ts);
                                                        intent1.putExtra("groupname", groupName);
                                                        intent1.putExtra("buttontext", "Leave");
                                                        startActivity(intent1);
                                                    }
                                                    // if is from the event, then back to the event
                                                    finish();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        final TimePickerDialog.OnTimeSetListener onStartTimeListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String AM_PM;
                int am_pm;

                hourAndMinutes.setText(String.format("%02d:%02d", hourOfDay, minute));
//                myCalendar.set(Calendar.HOUR, hourOfDay);
//                myCalendar.set(Calendar.MINUTE, minute);

            }
        };

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar);
            }

        };


        myd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(context, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        hourAndMinutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(context, onStartTimeListener, myCalendar
                        .get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE),
                        true).show();
            }
        });
    }

    private void updateLabel(Calendar myCalendar) {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        myd.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));
                latitude = Double.toString(point.latitude);
                longitude = Double.toString(point.longitude);
            }
        });

        // Keep these two at bottom
        updateLocationUI();
        getDeviceLocation();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                myLocation = null;
                getLocationPermission();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Exception: e.getMessage()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                LocationManager myLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                String currentProvider;
                // Get enabled location providers
                List<String> myProviders = myLocationManager.getProviders(true);

                // Choose appropriate location providers, the preference can be changed if necessary
                if (myProviders.contains(LocationManager.GPS_PROVIDER)) {
                    currentProvider = LocationManager.GPS_PROVIDER;
                } else if (myProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                    currentProvider = LocationManager.NETWORK_PROVIDER;
                } else {
                    // Enable location providers in the Android Setting if no location providers found
                    Toast.makeText(context, "No location providers! " +
                            "Enable location providers!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    return;
                }

                myLocation = myLocationManager.getLastKnownLocation(currentProvider);
                // Automatically updates the current location
                myLocationManager.requestLocationUpdates(currentProvider,
                        300000, 50, myListener);

                if (myLocation != null) {
                    // Set the map's camera position to the current location of the device.
                    display(myLocation);
                }
            } else {
                Toast.makeText(context, "Current location is null. Using defaults.",
                        Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(37.2643358, -121.787609), 1));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Exception: e.getMessage()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    Marker myLocationMarker;

    private void display(Location location) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()), 1));
        if (myLocationMarker != null) {
            myLocationMarker.remove();
        }
        myLocationMarker = mMap.addMarker(new MarkerOptions().
                position(new LatLng(location.getLatitude(),
                        location.getLongitude())));
    }

    // Listen to the location updates
    LocationListener myListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            display(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    // getLocationPermission
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(EventForm.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                EventForm.this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(EventForm.this, Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(EventForm.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_CODE);
        }
    }

    // Handle the permissions request response
    // Code is cited from https://developer.android.com/training/permissions/requesting
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length == 3
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                } else {
                    // Permission denied
                    Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        updateLocationUI();
    }
}