package com.myexample.privateeventconnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    RecyclerView mMessageRecycler;
    MessageListAdapter mMessageAdapter;
    List<BaseMessage> messageList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);



        messageList = new ArrayList<>();
        String text = "A message is a discrete unit of communication intended by the source for consumption by some recipient or group of recipients. A message may be delivered by various means, including courier, telegraphy, carrier pigeon and electronic bus. A message can be the content of a broadcast.";
        messageList.add(new BaseMessage("Toan", text, "12:58", "123"));
        messageList.add(new BaseMessage("Toan", "hello! hi! it is a very nice day", "12:58", "123"));
        messageList.add(new BaseMessage("Toan", text, "12:58", "123"));
        messageList.add(new BaseMessage("Toan", "hello! hi! it is a very nice day", "12:58", "123"));
        messageList.add(new BaseMessage("Toan", text, "12:58", "123"));
        messageList.add(new BaseMessage("Toan", "hello! hi! it is a very nice day", "12:58", "123"));
        messageList.add(new BaseMessage("Toan", text, "12:58", "123"));
        messageList.add(new BaseMessage("Toan", "hello! hi! it is a very nice day", "12:58", "123"));
        messageList.add(new BaseMessage("Toan", text, "12:58", "123"));
        messageList.add(new BaseMessage("Toan", "hello! hi! it is a very nice day", "12:58", "123"));
        messageList.add(new BaseMessage("Toan", text, "12:58", "123"));
        messageList.add(new BaseMessage("Toan", "hello! hi! it is a very nice day", "12:58", "123"));
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

        mMessageRecycler.scrollToPosition(messageList.size()-1);
    }
}