package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class MessageActivity extends AppCompatActivity {
    RecyclerView mMessageRecycler;
    MessageListAdapter mMessageAdapter;
    List<BaseMessage> messageList;
    EditText editText;
    Button btnSend;
    private DatabaseReference mDatabase;
    String eventtoken;
    String uid;
    String groupName;
    String myname;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        editText = findViewById(R.id.edittext_chatbox);
        btnSend = findViewById(R.id.button_chatbox_send);
        context = this;
        Intent intent = getIntent();
        groupName = intent.getStringExtra("groupname");
        eventtoken = intent.getStringExtra("eventtoken");
        myname = intent.getStringExtra("name");
        uid = intent.getStringExtra("uid");

        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Groups").child(groupName)
                .child("Events").child(eventtoken)
                .child("Messages");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString();
                if(content.isEmpty()){
                    Toast.makeText(context, "Input cannot be empty!", Toast.LENGTH_SHORT).show();
                }else{

                    Date date = new Date();
                    TimeZone.setDefault(TimeZone.getTimeZone("GMT-7"));
                    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                    date = cal.getTime();
                    long t = date.getTime();
                    final String timeStamp = new Timestamp(t).toString().
                            replace(".", "");
                    // start
//                    Calendar time = Calendar.getInstance();
//                    time.add(Calendar.MILLISECOND, -time.getTimeZone().getOffset(time.getTimeInMillis()));
//                    Date dt = time.getTime();
//                    long t = dt.getTime();
//                    final String timeStamp = new Timestamp(t).toString().
//                            replace(".", "");

                    // end

                    Log.d("timeStamp", timeStamp);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("name", myname);
                    hashMap.put("content", content);
                    hashMap.put("time", timeStamp);
                    hashMap.put("uid", uid);
                    mDatabase.child(timeStamp).setValue(hashMap);
                    editText.setText("");
                }
            }
        });


        messageList = new ArrayList<>();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for(DataSnapshot sn: snapshot.getChildren()){
                    BaseMessage msg = sn.getValue(BaseMessage.class);
                    messageList.add(msg);
                }
                mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
                mMessageAdapter = new MessageListAdapter(context, messageList);
                mMessageRecycler.setLayoutManager(new LinearLayoutManager(context));
                mMessageRecycler.setAdapter(mMessageAdapter);
                mMessageRecycler.scrollToPosition(messageList.size()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}