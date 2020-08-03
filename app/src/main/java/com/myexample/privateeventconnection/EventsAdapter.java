package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Set;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private Context mContext;
    private List<Event> events;
    private List<String> tokens;
    private String groupname;
    private Set<String> joined;
    private String uid;
    private FirebaseAuth mAuth;

    public EventsAdapter(Context mContext, List<Event> events, List<String> tokens, String groupname, Set<String> joined){
        this.mContext = mContext;
        this.events = events;
        this.tokens = tokens;
        this.groupname = groupname;
        this.joined = joined;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        this.uid = currentUser.getUid();
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
        final String token = tokens.get(position);
        if(joined.contains(token)){
            holder.join.setText("Leave");
        }else{
            holder.join.setText("Join");
        }
        holder.join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance()
                        .getReference("Users").child(uid).child("Groups")
                        .child(groupname);
                if(holder.join.getText().toString().equals("Leave")){
                        reference.child(token).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            holder.join.setText("Join");
                        }
                    });

                }else{
                        reference.child(token).setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            holder.join.setText("Leave");
                        }
                    });

                }
            }
        });
        holder.eventName.setText(event.EventName);
        holder.location.setText("Location: " + event.Location);
        holder.time.setText("Time: "+event.EventTime);
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //TODO IMPLEMNET event page
                Intent intent = new Intent(mContext, EventActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("groupname", groupname);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public Button join;
        public TextView eventName;
        public TextView location;
        public TextView time;

        public ViewHolder(View itemView){
            super(itemView);
            join = itemView.findViewById(R.id.joinEvent);
            eventName = itemView.findViewById(R.id.eventTitle);
            location = itemView.findViewById(R.id.eventloc);
            time = itemView.findViewById(R.id.eventtime);
            join = itemView.findViewById(R.id.joinEvent);

        }
    }
}