package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import java.util.List;
import java.util.Locale;

import java.util.Set;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private Context mContext;
    private List<Event> events;
    private String groupname;
    private Set<String> joined;
    private String uid;
    private FirebaseAuth mAuth;

    public EventsAdapter(Context mContext, List<Event> events, String groupname, Set<String> joined){
        this.mContext = mContext;
        this.events = events;
        this.groupname = groupname;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        this.uid = currentUser.getUid();
        this.joined = joined;
    }

    @NonNull
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_row, parent, false);
        return new EventsAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final EventsAdapter.ViewHolder holder, int position) {
        final Event event = events.get(position);
        final String token = event.getEventToken();

        if(joined.contains(token)){
            holder.join.setImageResource(R.drawable.baseline_remove_circle_outline_24);
            holder.join.setTag("Leave");
        }else{
            holder.join.setImageResource(R.drawable.baseline_add_circle_outline_24);
            holder.join.setTag("Join");
        }

        String myFormat = "MM/dd/yy, HH:mm"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        final Calendar myCalendar = Calendar.getInstance();
        String currentTime = sdf.format(myCalendar.getTime());
        Log.d("currenttime", currentTime);
        if(event.getEventTime().compareTo(currentTime) < 0){
            //these are past events
//            holder.itemView.setBackgroundColor(Color.parseColor("#FBE6D4"));
        }else{
            // upcoming events
//            holder.itemView.setBackgroundColor(Color.parseColor("#FECB89"));
        }

        holder.join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference = FirebaseDatabase.getInstance()
                        .getReference("Users").child(uid).child("Groups")
                        .child(groupname);



                if(holder.join.getTag().toString().equals("Leave")){

                    FirebaseDatabase.getInstance().getReference().child("Groups").child(groupname).child("Events").child(token).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Event event = dataSnapshot.child("EventInfo").getValue(Event.class);
                            if(event != null){
                                //current user is admin
                                if(uid.equals(event.getAdmin())){
                                    //prompt the dialog

                                    AlertDialog.Builder altdial = new AlertDialog.Builder(mContext);
                                    altdial.setMessage("You are the admin of this event. Do you want to dismiss?").setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {


                                                    //delete this event information from all users
                                                    deleteAllUsersEvents(groupname, token);

                                                    //delete this event information from all groups
                                                    FirebaseDatabase.getInstance().getReference().child("Groups").child(groupname).child("Events").child(token).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            //go to AfterLoginActivity
                                                            Intent intent = new Intent(mContext, AfterLoginActivity.class);
                                                            //set this activity to the top of stack, so users cannot go back to dismissed events
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            mContext.startActivity(intent);
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
                                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.getChildrenCount()>1){
                                                reference.child(token).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        holder.join.setImageResource(R.drawable.baseline_add_circle_outline_24);
                                                        holder.join.setTag("Join");
                                                    }
                                                });
                                            }else{
                                                reference.setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        holder.join.setImageResource(R.drawable.baseline_add_circle_outline_24);
                                                        holder.join.setTag("Join");
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
                    HashMap<String, String > hashMap = new HashMap<>();
                    hashMap.put("Admin", event.getAdmin());
                    hashMap.put("Description", event.getDescription());
                    hashMap.put("EventName", event.getEventName());
                    hashMap.put("EventTime", event.getEventTime());
                    hashMap.put("EventToken", event.getEventToken());
                    hashMap.put("Location", event.getLocation());
                    hashMap.put("Latitude", event.getLatitude());
                    hashMap.put("Longitude", event.getLongitude());
                    reference.child(token).child("EventInfo").setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//                        holder.join.setText("Leave");
                        holder.join.setImageResource(R.drawable.baseline_remove_circle_outline_24);
                        holder.join.setTag("Leave");
                    }
                });
                }
            }
        });
        holder.eventName.setText(event.EventName);
        holder.location.setText(event.Location);
        holder.time.setText(event.EventTime);
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, EventActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("groupname", groupname);
                intent.putExtra("buttontext", holder.join.getTag().toString());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView join;
        public TextView eventName;
        public TextView location;
        public TextView time;

        public ViewHolder(View itemView){
            super(itemView);
            join = itemView.findViewById(R.id.groupinfo_joinorleave);
            eventName = itemView.findViewById(R.id.groupinfo_eventTitle);
            location = itemView.findViewById(R.id.groupinfo_eventloc);
            time = itemView.findViewById(R.id.groupinfo_eventtime);
        }
    }

    private void deleteAllUsersEvents(final String groupName, final String token) {

        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot user : snapshot.getChildren()) {
                    if(user.child("Groups").child(groupName).getValue() != null){
                        if(user.child("Groups").child(groupName).child(token).getValue() != null) {
                            if (user.child("Groups").child(groupName).getChildrenCount() <= 1) {
                                // only one left
                                DatabaseReference rf = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                                rf.child("Groups").child(groupName).setValue(false);
                            } else {
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