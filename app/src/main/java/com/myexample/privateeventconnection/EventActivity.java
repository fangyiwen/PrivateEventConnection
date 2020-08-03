package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventActivity extends AppCompatActivity {
    TextView eventname;
    TextView time;
    TextView location;
    TextView description;
    Button join;
    Button chat;
    Context context;
    String name;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        chat = findViewById(R.id.chat);
        eventname = findViewById(R.id.eventname);
        time = findViewById(R.id.eventtime);
        location = findViewById(R.id.eventlocation);
        description = findViewById(R.id.desc);
        join = findViewById(R.id.eventjoin);
        context = this;
        //TODO ACCEPT EVENTNAME AND GROUPNAME
        Intent intent = getIntent();
        final String groupName = intent.getStringExtra("groupname");
        final String token = intent.getStringExtra("token");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Groups").child(groupName)
                .child("Events").child(token)
                .child("EventInfo");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("EventName").getValue().toString();
                String t = snapshot.child("EventTime").getValue().toString();
                String loc = snapshot.child("Location").getValue().toString();
                String desc = snapshot.child("Description").getValue().toString();
                eventname.setText(name);
                time.setText("Time: " + t);
                location.setText("Location: " + loc);
                description.setText(desc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child("Name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name!=null){
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra("groupname", groupName);
                    intent.putExtra("uid", uid);
                    intent.putExtra("eventtoken", token);
                    intent.putExtra("name", name);
                    startActivity(intent);
                }else{
                    Toast.makeText(context, "Please try again!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}