package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.List;

public class GroupOperationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    FirebaseUser currentUser;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_operation);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        // ListView for group list
        class Group {
            private String groupName;
            private String description;

            public Group(String groupName, String description) {
                this.groupName = groupName;
                this.description = description;
            }

            public String getGroupName() {
                return groupName;
            }

            public String getDescription() {
                return description;
            }
        }

        final ArrayList<Group> groupArray = new ArrayList<>();

        // Define a customized GroupAdapter
        class GroupAdapter extends ArrayAdapter<Group> {
            private int resourceId;

            public GroupAdapter(Context context, int listItemResourceId, List<Group> objects) {
                super(context, listItemResourceId, objects);
                resourceId = listItemResourceId;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                Group group = getItem(position);
                View view;
                ViewHolder viewHolder;

                if (convertView == null) {
                    view = LayoutInflater.from(getContext()).inflate(resourceId, null);

                    viewHolder = new ViewHolder();
                    viewHolder.groupName = view.findViewById(R.id.admin_textView_groupName);
                    viewHolder.description = view.findViewById(R.id.admin_textView_groupDescription);
                    viewHolder.deleteGroupButton = view.findViewById(R.id.admin_delGroup);
                    viewHolder.addUserButton = view.findViewById(R.id.admin_addUserInGroup);
                    viewHolder.removeUserButton = view.findViewById(R.id.admin_removeUserFromGroup);

                    // Default Group should never be operated.
                    if (groupArray.get(position).getGroupName().equals("DefaultGroup")) {
                        viewHolder.deleteGroupButton.setVisibility(View.GONE);
                        viewHolder.addUserButton.setVisibility(View.GONE);
                        viewHolder.removeUserButton.setVisibility(View.GONE);
                    }
                    view.setTag(viewHolder);
                } else {
                    view = convertView;
                    viewHolder = (ViewHolder) view.getTag();
                }

                viewHolder.groupName.setText(group.getGroupName());
                viewHolder.description.setText(group.getDescription());

                // Delete group from database
                viewHolder.deleteGroupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String groupName = groupArray.get(position).getGroupName();
                        final String description = groupArray.get(position).getDescription();
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot group : dataSnapshot.child("Groups").getChildren()) {
                                    if (group.getKey().equals(groupName)) {
                                        mDatabase.child("Groups").child(groupName).removeValue();
                                        break;
                                    }
                                }

                                for (DataSnapshot user : dataSnapshot.child("Users").getChildren()) {
                                    for (DataSnapshot group : user.child("Groups").getChildren()) {
                                        if (group.getKey().equals(groupName)) {
                                            mDatabase.child("Users").child(user.getKey()).child("Groups").child(groupName).removeValue();
                                            break;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                });

                // Add user to the group
                viewHolder.addUserButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String groupName = groupArray.get(position).getGroupName();
                        Intent intent = new Intent(GroupOperationActivity.this, AddUserToGroupActivity.class);
                        intent.putExtra("groupName", groupName);
                        intent.putExtra("add", true);
                        startActivity(intent);
                    }
                });

                // Remove user from the group
                viewHolder.removeUserButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String groupName = groupArray.get(position).getGroupName();
                        // RemoveUserFromGroupActivity uses AddUserToGroupActivity
                        Intent intent = new Intent(GroupOperationActivity.this, AddUserToGroupActivity.class);
                        intent.putExtra("groupName", groupName);
                        intent.putExtra("add", false);
                        startActivity(intent);
                    }
                });
                return view;
            }

            class ViewHolder {
                TextView groupName;
                TextView description;
                Button deleteGroupButton;
                Button addUserButton;
                Button removeUserButton;
            }
        }

        final GroupAdapter adapter = new GroupAdapter(this, R.layout.admin_group_row, groupArray);
        final ListView listView = findViewById(R.id.admin_listView_groupOperation);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupArray.clear();
                for (DataSnapshot kv : dataSnapshot.getChildren()) {
                    String groupName = kv.getKey();
                    String description = kv.child("GroupInfo").child("Description").getValue(String.class);
                    groupArray.add(new Group(groupName, description));
                }
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}