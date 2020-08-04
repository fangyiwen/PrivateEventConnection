package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EventActivity extends AppCompatActivity {
    TextView eventname;
    TextView time;
    TextView location;
    TextView description;
    Button join;
    Button chat;
    Context context;
    String name;
    ImageButton edit;
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
        edit = findViewById(R.id.edit);
        context = this;
        //TODO ACCEPT EVENTNAME AND GROUPNAME
        Intent intent = getIntent();
        final String groupName = intent.getStringExtra("groupname");
        final String token = intent.getStringExtra("token");
        final String btntext = intent.getStringExtra("buttontext");
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventForm.class);
                intent.putExtra("groupName", groupName);
                intent.putExtra("ts", token);
                startActivity(intent);
            }
        });
        //TODO 判断admin token 和 uid 是否一致 -> edit button 显示
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean admin = false;
                if(snapshot.child("Users").child(uid).child("Admin").getValue().toString().equals("1")){
                    admin = true;
                }
                if(snapshot.child("Groups").child(groupName).child("Events").child(token).child("EventInfo").child("Admin").getValue().equals(uid)){
                    admin = true;
                }
                if(!admin){
                    edit.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //TODO 判断这个event 是否还存在 如果不存在返回上一级

        join.setText(btntext);
        final DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("Groups")
                .child(groupName);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(join.getText().toString().equals("Leave")){
                    reference.child(token).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            join.setText("Join");
                        }
                    });

                }else{
                    mDatabase = FirebaseDatabase.getInstance().getReference()
                            .child("Groups").child(groupName).child("Events").child(token);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Event event = snapshot.child("EventInfo").getValue(Event.class);
                            HashMap<String, String > hashMap = new HashMap<>();
                            hashMap.put("Description", event.getDescription());
                            hashMap.put("Admin", event.getAdmin());
                            hashMap.put("EventName", event.getEventName());
                            hashMap.put("EventTime", event.getEventName());
                            hashMap.put("EventToken", event.getEventToken());
                            hashMap.put("Location", event.getLocation());
                            hashMap.put("Latitude", event.getLatitude());
                            hashMap.put("Longitude", event.getLongitude());
                            reference.child(token).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    join.setText("Leave");
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });



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
                if(join.getText().toString().equals("Join")){
                    Toast.makeText(context, "You need to join first!", Toast.LENGTH_SHORT).show();
                }else{
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
            }
        });

    }
}