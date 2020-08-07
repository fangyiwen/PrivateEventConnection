package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateNameActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String uid;
    private FirebaseUser currentUser;
    private Button updateName;
    private EditText newNameEditText;
    private TextView currentNameTextView, emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_name);

        updateName = findViewById(R.id.yf_button_ui);
        newNameEditText = findViewById(R.id.yf_editText_ui);
        currentNameTextView = findViewById(R.id.yf_textView7_ui2);
        emailTextView = findViewById(R.id.yf_textView3_ui);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Show email
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());
        }

        // Show name
        mDatabase.child("Users").child(uid).child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentNameTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Update name
        updateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateName.setEnabled(false);
                updateName.setText("Updating...");
                final String newName = newNameEditText.getText().toString();
                if (newName.trim().equals("")) {
                    updateName.setText("Submit");
                    updateName.setEnabled(true);
                    Toast.makeText(UpdateNameActivity.this, "New nick name must be at least one character with no Leading and trailing white spaces.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mDatabase.child("Users").child(uid).child("Name").setValue(newName);
                Snackbar.make(findViewById(android.R.id.content), "Updating finished", Snackbar.LENGTH_SHORT).show();
                updateName.setText("Submit");
                newNameEditText.setText("");
                updateName.setEnabled(true);
            }
        });
    }
}