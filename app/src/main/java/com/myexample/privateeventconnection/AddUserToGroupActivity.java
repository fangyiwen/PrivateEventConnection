package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddUserToGroupActivity extends AppCompatActivity {
    private String groupName;
    private boolean operation;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    FirebaseUser currentUser;
    String uid;
    private Context myContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);

        myContext = AddUserToGroupActivity.this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("groupName");
            operation = extras.getBoolean("add");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        // ListView for user list
        class User {
            private String email;
            private String name;
            private String uidDelete;

            public User(String email, String name, String uidDelete) {
                this.email = email;
                this.name = name;
                this.uidDelete = uidDelete;
            }

            public String getEmail() {
                return email;
            }

            public String getName() {
                return name;
            }

            public String getUidDelete() {
                return uidDelete;
            }
        }

        final ArrayList<User> userArray = new ArrayList<>();

        // Define a customized UserAdapter
        class UserAdapter extends ArrayAdapter<User> {
            private int resourceId;

            public UserAdapter(Context context, int listItemResourceId, List<User> objects) {
                super(context, listItemResourceId, objects);
                resourceId = listItemResourceId;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                User user = getItem(position);
                View view;
                ViewHolder viewHolder;

                if (convertView == null) {
                    view = LayoutInflater.from(myContext).inflate(resourceId, null);

                    viewHolder = new ViewHolder();
                    viewHolder.email = view.findViewById(R.id.admin_email);
                    viewHolder.name = view.findViewById(R.id.admin_nickName);
                    viewHolder.deleteButton = view.findViewById(R.id.admin_delUser);

                    view.setTag(viewHolder);
                } else {
                    view = convertView;
                    viewHolder = (ViewHolder) view.getTag();
                }

                viewHolder.email.setText(user.getEmail());
                viewHolder.name.setText(user.getName());
                if (operation) {
                    viewHolder.deleteButton.setImageResource(R.drawable.baseline_group_add_24);
                } else {
                    viewHolder.deleteButton.setImageResource(R.drawable.baseline_person_remove_24);
                    if (userArray.get(position).getUidDelete().equals(uid)) {
                        viewHolder.deleteButton.setVisibility(View.GONE);
                    }
                }

                // Add to / remove from the group in the database
                viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String uidDelete = userArray.get(position).getUidDelete();
                        mDatabase.child("Users").child(uidDelete).child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (operation) {
                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put(groupName, false);
                                    mDatabase.child("Users").child(uidDelete).child("Groups").updateChildren(childUpdates);
                                } else {
                                    mDatabase.child("Users").child(uidDelete).child("Groups").child(groupName).removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                });
                return view;
            }

            class ViewHolder {
                TextView email;
                TextView name;
                ImageView deleteButton;
            }
        }

        final UserAdapter adapter = new UserAdapter(myContext, R.layout.admin_user_row, userArray);
        final ListView listView = findViewById(R.id.admin_listView_delUser);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArray.clear();
                for (DataSnapshot kv : dataSnapshot.getChildren()) {
                    if (operation) {
                        if (!kv.child("Groups").hasChild(groupName)) {
                            String email = kv.child("Email").getValue(String.class);
                            String name = kv.child("Name").getValue(String.class);
                            String uidDelete = kv.getKey();
                            userArray.add(new User(email, name, uidDelete));
                        }
                    } else {
                        if (kv.child("Groups").hasChild(groupName)) {
                            String email = kv.child("Email").getValue(String.class);
                            String name = kv.child("Name").getValue(String.class);
                            String uidDelete = kv.getKey();
                            userArray.add(new User(email, name, uidDelete));
                        }

                    }
                }
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}