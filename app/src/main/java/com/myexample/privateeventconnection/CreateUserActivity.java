package com.myexample.privateeventconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CreateUserActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private EditText emailEditText, passwordEditText;
    private Button createUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        emailEditText = findViewById(R.id.editText_ui_createEmail);
        passwordEditText = findViewById(R.id.editText_ui_createPassword);
        createUserButton = findViewById(R.id.button_createUser);

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
                    createUserButton.setText("Submit");
                    createUserButton.setEnabled(true);
                    Toast.makeText(CreateUserActivity.this, "Email must be valid and password must be at least six characters with no white spaces.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                signUpWebApi(email, password);
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
                        mDatabase.child("Users").child(localId).child("Name").setValue("User (" + email.substring(0, email.indexOf("@")) + ")");

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
                            createUserButton.setText("Submit");
                            createUserButton.setEnabled(true);
                        }
                    });
                } catch (JSONException | IOException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Create user with Email: failure", Snackbar.LENGTH_SHORT).show();
                    Handler h4 = new Handler(Looper.getMainLooper());
                    h4.post(new Runnable() {
                        @Override
                        public void run() {
                            createUserButton.setText("Submit");
                            createUserButton.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }
}