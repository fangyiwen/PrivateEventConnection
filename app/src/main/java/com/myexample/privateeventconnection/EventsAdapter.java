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

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private Context mContext;
    private List<Event> events;

    public EventsAdapter(Context mContext, List<Event> events){
        this.mContext = mContext;
        this.events = events;
    }

    @NonNull
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_row, parent, false);
        return new EventsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsAdapter.ViewHolder holder, int position) {
        final Event event = events.get(position);
        holder.description.setText(event.description);
        holder.eventName.setText(event.eventName);
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //TODO IMPLEMNET event page
                Intent intent = new Intent(mContext, EventActivity.class);
                intent.putExtra("eventName", event.eventName);
                intent.putExtra("groupName", event.groupName);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView description;
        public Button join;
        public TextView eventName;

        public ViewHolder(View itemView){
            super(itemView);
            description = itemView.findViewById(R.id.eventDescription);
            join = itemView.findViewById(R.id.joinEvent);
            eventName = itemView.findViewById(R.id.eventTitle);
        }
    }
}