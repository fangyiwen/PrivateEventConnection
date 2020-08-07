package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateGroupActivity extends AppCompatActivity {
    private EditText groupEditText, groupDescriptionEditText;
    private Button createGroupButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button groupListButton;
    private String uid;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        createGroupButton = findViewById(R.id.createGroup_button3);
        groupEditText = findViewById(R.id.createGroup_editText3);
        groupDescriptionEditText = findViewById(R.id.createGroup_editText4);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Create group button
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupButton.setEnabled(false);
                createGroupButton.setText("Processing...");
                final String newGroup = groupEditText.getText().toString().trim();
                final String newGroupDescription = groupDescriptionEditText.getText().toString().trim();
                if (newGroup.equals("")) {
                    createGroupButton.setText("Submit");
                    createGroupButton.setEnabled(true);
                    Toast.makeText(CreateGroupActivity.this, "Group name must at least one characters with no leading and trailing spaces.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                mDatabase.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot group : dataSnapshot.getChildren()) {
                            if (group.getKey().equals(newGroup)){
                                Toast.makeText(CreateGroupActivity.this, "Failed: Group name " + newGroup + " already exists.",
                                        Toast.LENGTH_SHORT).show();
                                createGroupButton.setText("Submit");
                                createGroupButton.setEnabled(true);
                                return;
                            }
                        }
                        mDatabase.child("Groups").child(newGroup).child("GroupInfo").child("Admin").setValue("rCCaMwoxVuNIaNY08W2oR7Ih6lP2");
                        mDatabase.child("Groups").child(newGroup).child("GroupInfo").child("Description").setValue(newGroupDescription);
                        mDatabase.child("Users").child(uid).child("Groups").child(newGroup).setValue(false);

                        groupEditText.setText("");
                        groupDescriptionEditText.setText("");
                        createGroupButton.setText("Submit");
                        createGroupButton.setEnabled(true);
                        Snackbar.make(findViewById(android.R.id.content), "Successfully created the group " + newGroup, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
    }
}