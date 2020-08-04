package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GroupInfoActivity extends AppCompatActivity {
    private TextView groupnametx;
    private TextView groupDescription;
    private DatabaseReference mDatabase;
    private DatabaseReference reference;
    private DatabaseReference userReference;
    private Button joinorleave;
    private FirebaseAuth mAuth;
    private Parcelable recyclerViewState;
    RecyclerView recyclerView;
    EventsAdapter eventsAdapter;
    List<Event> mEvents;
    Set<String> joined;
    Context context;
    Button createEvent;
    String uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_group_info);
        groupnametx = findViewById(R.id.groupname);
        groupDescription = findViewById(R.id.groupDescription);
        createEvent = findViewById(R.id.createEvent);
        joinorleave = findViewById(R.id.joinEvent);
        joined = new HashSet<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();


        // Get the group name passed by GroupsFragment
        Intent intent = getIntent();
        final String groupname = intent.getStringExtra("groupname");
        // Fill event list
        recyclerView = findViewById(R.id.eventList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager  layoutManager = new LinearLayoutManager(GroupInfoActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        //-----
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventForm.class);
                intent.putExtra("groupName", groupname);
                intent.putExtra("ts", "");
                startActivity(intent);
            }
        });
        mEvents = new ArrayList<Event>();
        assert groupname != null;
        reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                joined.clear();
                mEvents.clear();
                for(DataSnapshot sn: snapshot.getChildren()){
                    if(sn.getKey().equals("Users")){
                        for(DataSnapshot tk: sn.child(uid).child("Groups").child(groupname).getChildren()){
                            //TODO not checking dummy joined events, but it's ok so far
                            joined.add(tk.getKey());
                        }
                    }
//                    joined.add(sn.getKey());
                    if(sn.getKey().equals("Groups")){
                        for(DataSnapshot tk: sn.child(groupname).child("Events").getChildren()){
                            Event event = tk.child("EventInfo").getValue(Event.class);
                            if(event!=null && !event.getLatitude().equals("-92")){
                                mEvents.add(event);
                            }


                        }
                    }
                }
                if(!mEvents.isEmpty()){
                    Collections.sort(mEvents, new Comparator<Event>() {
                        @Override
                        public int compare(Event o1, Event o2) {
                            return o2.getEventTime().compareTo(o1.getEventTime());
                        }
                    });
                }

                //to restore the position before click
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerView.getLayoutManager().onSaveInstanceState());
                eventsAdapter = new EventsAdapter(context, mEvents, groupname, joined);
                recyclerView.setAdapter(eventsAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //-----
        // Set group name
        this.groupnametx.setText(groupname);
        // Retrieve an instance of database using reference the location
        mDatabase = FirebaseDatabase.getInstance().getReference("Groups").child(groupname).child("GroupInfo");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get description of this group from database
                String description = dataSnapshot.child("Description").getValue().toString();
                // Set description
                groupDescription.setText(description);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}