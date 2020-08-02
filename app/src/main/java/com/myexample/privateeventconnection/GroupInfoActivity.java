package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupInfoActivity extends AppCompatActivity {
    private TextView groupname;
    private TextView groupDescription;
    private DatabaseReference mDatabase;
    RecyclerView recyclerView;
    EventsAdapter eventsAdapter;
    List<Event> mEvents;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_group_info);
        groupname = findViewById(R.id.groupname);
        groupDescription = findViewById(R.id.groupDescription);


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

        //TODO READ MEVENTS FROM DB
        mEvents = new ArrayList<Event>();
        String text = "Lining up plans in San Jose? Whether you're a local, new in town, or just passing through, you'll be sure to find something on Eventbrite that piques your ...";
        mEvents.add(new Event("Event Name", "Group Name", text));
        mEvents.add(new Event("Event Name", "Group Name", text));
        mEvents.add(new Event("Event Name", "Group Name", text));
        mEvents.add(new Event("Event Name", "Group Name", "aaabbbbdbcc"));
        eventsAdapter = new EventsAdapter(context, mEvents);
        recyclerView.setAdapter(eventsAdapter);

        // Set group name
        this.groupname.setText(groupname);
        // Retrieve an instance of database using reference the location
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get description of this group from database
                String description = dataSnapshot.child("Groups").child(groupname).child("GroupInfo").child("Description").getValue().toString();
                // Set description
                groupDescription.setText(description);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }
}