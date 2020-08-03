package com.myexample.privateeventconnection;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.home_fragment, container, false);

        // Code referenced from https://www.jianshu.com/p/0a72276c537f
        // Define a class for the ListView adapter
        class HomeEvent {
            private String eventName;
            private String eventTime;
            private String location;
            private String groupname;
            private String token;

            public HomeEvent(String eventName, String eventTime, String location, String groupname, String token) {
                this.eventName = eventName;
                this.eventTime = eventTime;
                this.location = location;
                this.groupname = groupname;
                this.token = token;
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
        }

        final ArrayList<HomeEvent> eventArray = new ArrayList<>();
        // Loading...
        final HomeEvent loadEvent = new HomeEvent("Loading...", "", "", "", "");
        eventArray.add(loadEvent);

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
                    view = LayoutInflater.from(getContext()).inflate(resourceId, null);

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

        final EventAdapter adapter = new EventAdapter(getContext(), R.layout.home_event_row, eventArray);
        ListView listView = (ListView) view.findViewById(R.id.home_listView);
        listView.setAdapter(adapter);

        // Click to direct to the activity for the detailed event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HomeEvent homeEvent = eventArray.get(position);
                // Check if it is loading
                if (homeEvent.getGroupname().equals("")){
                    return;
                }
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("token", homeEvent.getToken());
                intent.putExtra("groupname", homeEvent.getGroupname());
                startActivity(intent);
            }
        });

        // Download data from database
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventArray.clear();
                for (DataSnapshot group : dataSnapshot.getChildren()) {
                    for (DataSnapshot event : group.getChildren()) {
                        String eventName = event.child("EventInfo").child("EventName").getValue(String.class);
                        String eventTime = event.child("EventInfo").child("EventTime").getValue(String.class);
                        String location = event.child("EventInfo").child("Location").getValue(String.class);
                        String groupname = group.getKey();
                        String token = event.getKey();
                        HomeEvent homeEvent = new HomeEvent(eventName, eventTime, location, groupname, token);
                        eventArray.add(homeEvent);
                    }
                }
                ListView listView = (ListView) view.findViewById(R.id.home_listView);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

//        holder.itemView.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                //TODO IMPLEMNET event page
//                Intent intent = new Intent(mContext, EventActivity.class);
//                intent.putExtra("token", token);
//                intent.putExtra("groupname", groupname);
//                mContext.startActivity(intent);
//            }
//        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }

}