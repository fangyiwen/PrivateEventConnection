package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText emailEditText, passwordEditText, groupEditText, groupDescriptionEditText;
    private Button createUserButton, userListButton, createGroupButton, groupListButton;
    private String uid;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        emailEditText = findViewById(R.id.admin_editText);
        passwordEditText = findViewById(R.id.admin_editText2);
        createUserButton = findViewById(R.id.admin_button);
        userListButton = findViewById(R.id.admin_button2);
        createGroupButton = findViewById(R.id.admin_button3);
        groupEditText = findViewById(R.id.admin_editText3);
        groupDescriptionEditText = findViewById(R.id.admin_editText4);
        groupListButton = findViewById(R.id.admin_button4);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Create user button
        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUserButton.setEnabled(false);
                createUserButton.setText("Processing...");
                final String email = emailEditText.getText().toString();
                final String password = passwordEditText.getText().toString();
                if (email.length() < 5 || password.length() < 6 || !password.replaceAll("\\s", "").equals(password)) {
                    createUserButton.setText("Create User");
                    createUserButton.setEnabled(true);
                    Toast.makeText(AdminActivity.this, "Email must be valid and password must be at least six characters with no white spaces.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                signUpWebApi(email, password);
            }
        });

        // Open user list button for deleting
        userListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, DeleteUserActivity.class);
                startActivity(intent);
            }
        });

        // Open group list button for operations
        groupListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, GroupOperationActivity.class);
                startActivity(intent);
            }
        });

        // Create group button
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupButton.setEnabled(false);
                createGroupButton.setText("Processing...");
                final String newGroup = groupEditText.getText().toString().trim();
                final String newGroupDescription = groupDescriptionEditText.getText().toString().trim();
                if (newGroup.equals("")) {
                    createGroupButton.setText("Create Group");
                    createGroupButton.setEnabled(true);
                    Toast.makeText(AdminActivity.this, "Group name must at least one characters with no leading and trailing spaces.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                mDatabase.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot group : dataSnapshot.getChildren()) {
                            if (group.getKey().equals(newGroup)){
                                Toast.makeText(AdminActivity.this, "Failed: Group name " + newGroup + " already exists.",
                                        Toast.LENGTH_SHORT).show();
                                createGroupButton.setText("Create Group");
                                createGroupButton.setEnabled(true);
                                return;
                            }
                        }
                        mDatabase.child("Groups").child(newGroup).child("GroupInfo").child("Admin").setValue("rCCaMwoxVuNIaNY08W2oR7Ih6lP2");
                        mDatabase.child("Groups").child(newGroup).child("GroupInfo").child("Description").setValue(newGroupDescription);
                        mDatabase.child("Users").child(uid).child("Groups").child(newGroup).setValue(false);

                        groupEditText.setText("");
                        groupDescriptionEditText.setText("");
                        createGroupButton.setText("Create Group");
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

    // Create a new email and password user by HTTP POST request
    // https://firebase.google.com/docs/reference/rest/auth
    public void signUpWebApi(final String email, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                try {
                    String API_KEY = "AIzaSyC3-GY_gyLD2S1P794QXNT7pO7YkrIZw8Y";
                    jPayload.put("email", email);
                    jPayload.put("password", password);
                    jPayload.put("returnSecureToken", true);

                    URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(jPayload.toString().getBytes());
                    outputStream.close();

                    InputStream inputStream = conn.getInputStream();
                    Scanner myScanner = new Scanner(inputStream).useDelimiter("\\A");
                    String response = myScanner.hasNext() ? myScanner.next() : "";

                    JSONObject myJsonObject = new JSONObject(response);
                    // localId	is the uid of the newly created user
                    String localId = myJsonObject.getString("localId");

                    if (conn.getResponseCode() == 200) {
                        mDatabase.child("Users").child(localId).child("Password").setValue(password);
                        mDatabase.child("Users").child(localId).child("Admin").setValue(0);
                        mDatabase.child("Users").child(localId).child("Groups").child("DefaultGroup").setValue(false);
                        mDatabase.child("Users").child(localId).child("Email").setValue(email);
                        mDatabase.child("Users").child(localId).child("Name").setValue("User (" + (email) + ")");

                        Snackbar.make(findViewById(android.R.id.content), "Create user with Email: success with Email " + email + " and password " + password, Snackbar.LENGTH_SHORT).show();
                        Handler h2 = new Handler(Looper.getMainLooper());
                        h2.post(new Runnable() {
                            @Override
                            public void run() {
                                emailEditText.setText("");
                                passwordEditText.setText("");
                            }
                        });
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Create user with Email: failure", Snackbar.LENGTH_SHORT).show();
                    }
                    Handler h3 = new Handler(Looper.getMainLooper());
                    h3.post(new Runnable() {
                        @Override
                        public void run() {
                            createUserButton.setText("Create User");
                            createUserButton.setEnabled(true);
                        }
                    });
                } catch (JSONException | IOException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Create user with Email: failure", Snackbar.LENGTH_SHORT).show();
                    Handler h4 = new Handler(Looper.getMainLooper());
                    h4.post(new Runnable() {
                        @Override
                        public void run() {
                            createUserButton.setText("Create User");
                            createUserButton.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }
}