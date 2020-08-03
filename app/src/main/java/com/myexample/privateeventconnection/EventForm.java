package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.sql.Timestamp;
import java.util.Date;

public class EventForm extends AppCompatActivity {

    MaterialEditText eventName;
    MaterialEditText location;
    MaterialEditText description;
    MaterialAutoCompleteTextView myd;
    MaterialAutoCompleteTextView hourAndMinutes;
    Button submit_btn;
    DatabaseReference reference;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        eventName = findViewById(R.id.eventformname);
        location = findViewById(R.id.eventformlocation);
        description = findViewById(R.id.eventformdesc);
        myd = findViewById(R.id.eventformdate);
        hourAndMinutes = findViewById(R.id.eventformtime);
        submit_btn = findViewById(R.id.formsubmit);
        context = this;

        Intent intent = getIntent();
        final String groupName = intent.getStringExtra("groupName");

        reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupName).child("Events");
        final Calendar myCalendar = Calendar.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String userID = currentUser.getUid();
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = eventName.getText().toString();
                String loc = location.getText().toString();
                String desc = description.getText().toString();
                String ddate = myd.getText().toString();
                String hm = hourAndMinutes.getText().toString();
                String time = ddate + ", " + hm;
                if(name.isEmpty()){
                    Toast.makeText(EventForm.this, "Event name is required!", Toast.LENGTH_SHORT).show();
                }else if(loc.isEmpty()){
                    Toast.makeText(EventForm.this, "Location is required!", Toast.LENGTH_SHORT).show();
                }else if(desc.isEmpty()){
                    Toast.makeText(EventForm.this, "Description is required!", Toast.LENGTH_SHORT).show();
                }else if(ddate.isEmpty()){
                    Toast.makeText(EventForm.this, "Date is required!", Toast.LENGTH_SHORT).show();
                }else if(hm.isEmpty()){
                    Toast.makeText(EventForm.this, "Time is required!", Toast.LENGTH_SHORT).show();
                }else{
                    final HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("EventName", name);
                    hashMap.put("Admin", userID);
                    hashMap.put("Description", desc);
                    hashMap.put("Location", loc);
                    hashMap.put("EventTime", time);
                    Date dt = new Date();
                    long t = dt.getTime();
                    final String ts = new Timestamp(t).toString().replace("-", "").
                            replace(" ", "").
                            replace(":", "").
                            replace(".", "");
                    HashMap<String, String> users = new HashMap<>();
                    users.put(userID, "1");
                    reference.child(ts).child("EventUsers").setValue(users, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error != null){
                                Toast.makeText(context, "Event creation failed. Code 1", Toast.LENGTH_SHORT).show();
                            }else{
                                reference.child(ts).child("EventInfo").setValue(hashMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if(error != null){
                                            Toast.makeText(context, "Event creation failed. Code 2", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Intent intent1 = new Intent(context, GroupInfoActivity.class);
                                            intent1.putExtra("groupname", groupName);
                                            startActivity(intent1);
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    });


                }


            }
        });

        final TimePickerDialog.OnTimeSetListener onStartTimeListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String AM_PM;
                int am_pm;

                hourAndMinutes.setText(String.format("%02d:%02d" , hourOfDay, minute));
//                myCalendar.set(Calendar.HOUR, hourOfDay);
//                myCalendar.set(Calendar.MINUTE, minute);

            }
        };

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar);
            }

        };


        myd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(context, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        hourAndMinutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(context, onStartTimeListener, myCalendar
                        .get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE),
                        true).show();
            }
        });
    }

    private void updateLabel(Calendar myCalendar) {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        myd.setText(sdf.format(myCalendar.getTime()));
    }
}