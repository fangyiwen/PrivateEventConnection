package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class EventActivity extends AppCompatActivity {
    TextView eventname;
    TextView time;
    TextView location;
    TextView description;
    TextView join;
    ImageButton chat;
    Context context;
    String name;
    TextView edit;
    Button showmore;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    String uid;
    boolean admin;

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
        showmore = findViewById(R.id.btShowmore);
        context = this;
        Intent intent = getIntent();
        final String groupName = intent.getStringExtra("groupname");
        final String token = intent.getStringExtra("token");
        final String btntext = intent.getStringExtra("buttontext");


        showmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (showmore.getText().toString().equalsIgnoreCase("Show more..."))
                {
                    description.setMaxLines(Integer.MAX_VALUE);//your TextView
                    showmore.setText("Show less");
                }
                else
                {
                    description.setMaxLines(5);//your TextView
                    showmore.setText("Show more...");
                }
            }
        });
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventForm.class);
                intent.putExtra("groupName", groupName);
                intent.putExtra("ts", token);
                startActivity(intent);
            }
        });

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean admin = false;
                if(snapshot.child("Admin").getValue(Double.class).equals(1)){
                    admin = true;
                }
                if(snapshot.child("Groups").child(groupName).child(token).hasChildren() && snapshot.child("Groups").child(groupName).child(token).child("EventInfo").child("Admin").getValue().equals(uid)){
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


        join.setText(btntext);
        if(btntext.equals("Join")){
            join.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_add_circle_outline_24, 0, 0,0);
        }else{
            join.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_remove_circle_outline_24, 0, 0, 0);
        }

        final DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("Groups")
                .child(groupName);




        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Groups").child(groupName)
                .child("Events").child(token)
                .child("EventInfo");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name = snapshot.child("EventName").getValue().toString();
                    String t = snapshot.child("EventTime").getValue().toString();
                    String loc = snapshot.child("Location").getValue().toString();
                    String desc = snapshot.child("Description").getValue().toString();
                    eventname.setText(name);
                    time.setText("Time: " + t);
                    location.setText("Location: " + loc);
                    description.setText(desc);
                }
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

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                admin = false;
                if(snapshot.child("Admin").getValue(Double.class).equals(1)){
                    admin = true;
                }

                if(snapshot.child("Groups").child(groupName).child(token).hasChildren() && snapshot.child("Groups").child(groupName).child(token).child("EventInfo").child("Admin").getValue().equals(uid)){
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


        // If non-admin users are still in this activity but this event has been dismissed,
        // send users to afterlogin activity

        if(!admin){
            FirebaseDatabase.getInstance()
                    .getReference("Groups").child(groupName).child("Events").
                    child(token).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() == null ){
                        //go to AfterLoginActivity
                        Intent intent = new Intent(EventActivity.this, AfterLoginActivity.class);
                        //set this activity to the top of stack, so users cannot go back to dismissed events
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);



                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        join.setText(btntext);
        // If current user is admin, we need to dismiss the event and
        // delete this event's information from database.
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(join.getText().toString().equals("Leave")){
                    FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName).child("Events").child(token).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Event event = dataSnapshot.child("EventInfo").getValue(Event.class);
                            if(event != null){
                                //current user is admin
                                if(uid.equals(event.getAdmin())){
                                    //prompt the dialog
                                    Log.d("iiisadmin", "admin");

                                    AlertDialog.Builder altdial = new AlertDialog.Builder(EventActivity.this);
                                    altdial.setMessage("You are the admin of this event. Do you want to dismiss?").setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {


                                                    //delete this event information from all users
                                                    deleteAllUsersEvents(groupName, token);

                                                    //delete this event information from all groups
                                                    FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName).child("Events").child(token).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            //go to AfterLoginActivity
                                                            Intent intent = new Intent(EventActivity.this, AfterLoginActivity.class);
                                                            //set this activity to the top of stack, so users cannot go back to dismissed events
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                        }
                                                    });

                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog alertDialog = altdial.create();
                                    alertDialog.setTitle("Warning");
                                    alertDialog.show();

                                }
                                // current user is not admin
                                else {
                                    final DatabaseReference rf = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.getChildrenCount()>1){
                                                rf.child("Groups").child(groupName).child(token).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        join.setText("Join");
                                                        join.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_add_circle_outline_24, 0, 0,0);
                                                    }
                                                });
                                            }else{
                                                Log.d("countttt2", String.valueOf(snapshot.getChildrenCount()));
                                                rf.child("Groups").child(groupName).setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                join.setText("Join");
                                                                join.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_add_circle_outline_24, 0, 0,0);
                                                    }
                                                });
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }



                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });





                }else{
                    mDatabase = FirebaseDatabase.getInstance().getReference()
                            .child("Groups").child(groupName).child("Events").child(token);



                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {



                            //check null
                            if(snapshot.child("EventInfo").getValue() != null){
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
                                reference.child(token).child("EventInfo").setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        join.setText("Leave");
                                        join.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_remove_circle_outline_24, 0, 0,0);
                                    }
                                });
                            }


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
                // when we delete the event, we will run this function
                // so we need to determine if value is null
                if(snapshot.child("EventName").getValue() != null){
                    String name = snapshot.child("EventName").getValue().toString();
                    String t = snapshot.child("EventTime").getValue().toString();
                    String loc = snapshot.child("Location").getValue().toString();
                    String desc = snapshot.child("Description").getValue().toString();
                    eventname.setText(name);
                    time.setText("Time: " + t);
                    location.setText("Location: " + loc);
                    description.setText(desc);
                }

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

    private void deleteAllUsersEvents(final String groupName, final String token) {

        FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot user : snapshot.getChildren()) {
                    if(user.child("Groups").child(groupName).getValue() != null){
                        if(user.child("Groups").child(groupName).child(token).getValue() != null){
                            if(user.child("Groups").child(groupName).getChildrenCount()<=1){
                                // only one left
                                DatabaseReference rf = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                                rf.child("Groups").child(groupName).setValue(false);
                            }else{
                                //more than one
                                user.child("Groups").child(groupName).child(token).getRef().removeValue();
                            }
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}