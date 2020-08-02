package com.myexample.privateeventconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EventActivity extends AppCompatActivity {
    Button chat;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        chat = findViewById(R.id.chat);
        context = this;
        //TODO ACCEPT EVENTNAME AND GROUPNAME
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
//        intent.putExtra("eventName",groupNames.get(position));
                startActivity(intent);
            }
        });

    }
}