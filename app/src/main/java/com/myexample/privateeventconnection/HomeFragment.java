package com.myexample.privateeventconnection;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private HomeViewModel mViewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final int MY_PERMISSIONS_REQUEST_CODE = 1;
    private TextView textViewHello;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private CircleImageView profileCircleImageView2;
    private final long ONE_MEGABYTE = 20 * 1024 * 1024;
    private Context myContext;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        myContext = getContext();

        final View view = inflater.inflate(R.layout.home_fragment, container, false);

        textViewHello = view.findViewById(R.id.home_textView_hello);

        // Google map SDK
        // https://developers.google.com/maps/documentation/android-sdk/map-with-marker
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.home_map);
        mapFragment.getMapAsync(this);
        // Construct a FusedLocationProviderClient.

        // Code referenced from https://www.jianshu.com/p/0a72276c537f
        // Define a class for the ListView adapter
        class HomeEvent {
            private String eventName;
            private String eventTime;
            private String location;
            private String groupname;
            private String token;
            private String latitude;
            private String longitude;

            public HomeEvent(String eventName, String eventTime, String location, String groupname, String token, String latitude, String longitude) {
                this.eventName = eventName;
                this.eventTime = eventTime;
                this.location = location;
                this.groupname = groupname;
                this.token = token;
                this.latitude = latitude;
                this.longitude = longitude;
            }

            public String getEventName() {
                return eventName;
            }

            public String getEventTime() {
                return eventTime;
            }

            public String getLocation() {
                return location;
            }

            public String getGroupname() {
                return groupname;
            }

            public String getToken() {
                return token;
            }

            public String getLatitude() {
                return latitude;
            }

            public String getLongitude() {
                return longitude;
            }
        }

        final ArrayList<HomeEvent> eventArray = new ArrayList<>();

        // Define a customized EventAdapter
        class EventAdapter extends ArrayAdapter<HomeEvent> {
            private int resourceId;

            public EventAdapter(Context context, int listItemResourceId, List<HomeEvent> objects) {
                super(context, listItemResourceId, objects);
                resourceId = listItemResourceId;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                HomeEvent event = getItem(position);
                View view;
                ViewHolder viewHolder;

                if (convertView == null) {
                    view = LayoutInflater.from(myContext).inflate(resourceId, null);

                    viewHolder = new ViewHolder();
                    viewHolder.eventName = (TextView) view.findViewById(R.id.home_eventTitle);
                    viewHolder.eventTime = (TextView) view.findViewById(R.id.home_eventtime);
                    viewHolder.location = (TextView) view.findViewById(R.id.home_eventloc);
                    view.setTag(viewHolder);
                } else {
                    view = convertView;
                    viewHolder = (ViewHolder) view.getTag();
                }

                viewHolder.eventName.setText(event.getEventName());
                viewHolder.eventTime.setText(event.getEventTime());
                viewHolder.location.setText(event.getLocation());
                return view;
            }

            class ViewHolder {
                TextView eventName;
                TextView eventTime;
                TextView location;
            }
        }

        final EventAdapter adapter = new EventAdapter(myContext, R.layout.home_event_row, eventArray);
        final ListView listView = view.findViewById(R.id.home_listView);
        listView.setAdapter(adapter);

        // Click to direct to the activity for the detailed event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HomeEvent homeEvent = eventArray.get(position);
                // Check if it is loading
                if (homeEvent.getGroupname().equals("")) {
                    return;
                }
                Intent intent = new Intent(myContext, EventActivity.class);
                intent.putExtra("token", homeEvent.getToken());
                intent.putExtra("groupname", homeEvent.getGroupname());
                intent.putExtra("buttontext", "Leave");
                startActivity(intent);
            }
        });

        // Download data from database
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Show name
        mDatabase.child("Users").child(uid).child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textViewHello.setText("Hello " + dataSnapshot.getValue(String.class) + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Show avatar
        // https://firebase.google.com/docs/storage/android/download-files
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("avatars/" + uid + ".jpg");

        profileCircleImageView2 = view.findViewById(R.id.profileCircleImageView2);
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(myContext)
                        .load(bytes)
                        .into(profileCircleImageView2);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                profileCircleImageView2.setImageResource(R.drawable.default_avatar);
            }
        });


        mDatabase.child("Users").child(uid).child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventArray.clear();
                for (DataSnapshot group : dataSnapshot.getChildren()) {
                    if (!group.hasChildren()) {
                        continue;
                    }
                    for (DataSnapshot event : group.getChildren()) {
                        String eventName = event.child("EventInfo").child("EventName").getValue(String.class);
                        String eventTime = event.child("EventInfo").child("EventTime").getValue(String.class);
                        String location = event.child("EventInfo").child("Location").getValue(String.class);
                        String latitude = event.child("EventInfo").child("Latitude").getValue(String.class);
                        String longitude = event.child("EventInfo").child("Longitude").getValue(String.class);
                        String groupname = group.getKey();
                        String token = event.getKey();
                        HomeEvent homeEvent = new HomeEvent(eventName, eventTime, location, groupname, token, latitude, longitude);
                        eventArray.add(homeEvent);
                    }
                }

                if (!eventArray.isEmpty()) {
                    Collections.sort(eventArray, new Comparator<HomeEvent>() {
                        @Override
                        public int compare(HomeEvent o1, HomeEvent o2) {
                            String[] dt1 = o1.getEventTime().split(", ");
                            String[] dt2 = o2.getEventTime().split(", ");
                            String[] date1 = dt1[0].split("/");
                            String[] date2 = dt2[0].split("/");
                            String dateandtime1 = date1[2] + date1[0] + date1[1] + dt1[1];
                            String dateandtime2 = date2[2] + date2[0] + date2[1] + dt2[1];
                            return dateandtime2.compareTo(dateandtime1);
                        }
                    });
                }

                listView.setAdapter(adapter);

                // Update map
                mMap.clear();
                if (myLocation != null) {
                    mMap.addMarker(new MarkerOptions().
                            position(new LatLng(myLocation.getLatitude(),
                                    myLocation.getLongitude())).title("Me").icon(BitmapDescriptorFactory.
                            defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }

                for (HomeEvent homeEvent : eventArray) {
                    mMap.addMarker(new MarkerOptions().
                            position(new LatLng(Double.parseDouble(homeEvent.getLatitude()),
                                    Double.parseDouble(homeEvent.getLongitude())))
                            .title(homeEvent.getEventName() + " @ "
                                    + homeEvent.getEventTime()));
                }
                if (myLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(myLocation.getLatitude(),
                                    myLocation.getLongitude()), 2));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }

    GoogleMap mMap;
    Location myLocation;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        // Keep at bottom
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    myContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(myContext, Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocationHelper();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_CODE);
            }
        } catch (Exception e) {
            Toast.makeText(myContext, "Exception: e.getMessage()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getDeviceLocationHelper() {
        LocationManager myLocationManager = (LocationManager) myContext.getSystemService(LOCATION_SERVICE);
        String currentProvider;
        // Get enabled location providers
        List<String> myProviders = myLocationManager.getProviders(true);

        // Choose appropriate location providers, the preference can be changed if necessary
        if (myProviders.contains(LocationManager.GPS_PROVIDER)) {
            currentProvider = LocationManager.GPS_PROVIDER;
        } else if (myProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            currentProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            return;
        }

        myLocation = myLocationManager.getLastKnownLocation(currentProvider);
        // Automatically updates the current location
        myLocationManager.requestLocationUpdates(currentProvider,
                60000, 50, myListener);

        if (myLocation != null) {
            // Set the map's camera position to the current location of the device.
            display(myLocation);
        }
    }

    Marker myLocationMarker;

    private void display(Location location) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()), 2));
        if (myLocationMarker != null) {
            myLocationMarker.remove();
        }
        myLocationMarker = mMap.addMarker(new MarkerOptions().
                position(new LatLng(location.getLatitude(),
                        location.getLongitude())).title("Me").icon(BitmapDescriptorFactory.
                defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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

    // Handle the permissions request response
    // Code is cited from https://developer.android.com/training/permissions/requesting
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length == 3
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocationHelper();
                } else {
                    // Permission denied
                    Toast.makeText(myContext, "Permission denied! Current location is null. Using defaults.",
                            Toast.LENGTH_SHORT).show();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(37.2643358, -121.787609), 2));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
            break;
        }
    }
}