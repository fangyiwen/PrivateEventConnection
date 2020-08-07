package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdatePasswordActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String uid;
    private FirebaseUser currentUser;
    private Button updatePassword;
    private EditText newPasswordEditText, oldPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        updatePassword = findViewById(R.id.yf_button2_ui);
        newPasswordEditText = findViewById(R.id.yf_editText2_ui);
        oldPasswordEditText = findViewById(R.id.yf_editText3_ui);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Update password
        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword.setEnabled(false);
                updatePassword.setText("Updating...");
                final String newPassword = newPasswordEditText.getText().toString();
                if (newPassword.length() < 6 || !newPassword.replaceAll("\\s", "").equals(newPassword)) {
                    updatePassword.setText("Submit");
                    updatePassword.setEnabled(true);
                    Toast.makeText(UpdatePasswordActivity.this, "New password must be at least six characters with no white spaces.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider
                        .getCredential(currentUser.getEmail(), oldPasswordEditText.getText().toString());
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    currentUser.updatePassword(newPassword)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Snackbar.make(findViewById(android.R.id.content), "Password updating successful", Snackbar.LENGTH_SHORT).show();
                                                        mDatabase.child("Users").child(uid).child("Password").setValue(newPassword);
                                                    } else {
                                                        Snackbar.make(findViewById(android.R.id.content), "Password updating failed", Snackbar.LENGTH_SHORT).show();
                                                    }
                                                    newPasswordEditText.setText("");
                                                    oldPasswordEditText.setText("");
                                                    updatePassword.setText("Submit");
                                                    updatePassword.setEnabled(true);
                                                }
                                            });
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content), "Authentication failed", Snackbar.LENGTH_SHORT).show();
                                    updatePassword.setEnabled(true);
                                }
                            }
                        });
            }
        });
    }
}