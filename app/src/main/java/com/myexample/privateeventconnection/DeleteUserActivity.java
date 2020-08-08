package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeleteUserActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    FirebaseUser currentUser;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        // ListView for user list
        class User {
            private String email;
            private String name;
            private String password;
            private String uidDelete;

            public User(String email, String name, String password, String uidDelete) {
                this.email = email;
                this.name = name;
                this.password = password;
                this.uidDelete = uidDelete;
            }

            public String getEmail() {
                return email;
            }

            public String getName() {
                return name;
            }

            public String getPassword() {
                return password;
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
                    view = LayoutInflater.from(getContext()).inflate(resourceId, null);

                    viewHolder = new ViewHolder();
                    viewHolder.email = view.findViewById(R.id.admin_email);
                    viewHolder.name = view.findViewById(R.id.admin_nickName);
                    viewHolder.deleteButton = view.findViewById(R.id.admin_delUser);
                    // Admin role should never be deleted
                    if (userArray.get(position).getEmail().equals("admin@example.com")) {
                        viewHolder.deleteButton.setVisibility(View.GONE);
                    }
                    view.setTag(viewHolder);
                } else {
                    view = convertView;
                    viewHolder = (ViewHolder) view.getTag();
                }

                viewHolder.email.setText(user.getEmail());
                viewHolder.name.setText(user.getName());

                // Delete from database
                viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String email = userArray.get(position).getEmail();
                        final String password = userArray.get(position).getPassword();
                        final String uidDelete = userArray.get(position).getUidDelete();
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot group : dataSnapshot.child("Groups").getChildren()) {
                                    if (!group.hasChild("Events")) {
                                        continue;
                                    }
                                    for (DataSnapshot event : group.child("Events").getChildren()) {
                                        if (event.child("EventInfo").child("Admin").equals(uidDelete)) {
                                            mDatabase.child("Groups").child(group.getKey()).child("Events").child(event.getKey()).child("EventInfo").child("Admin").setValue("rCCaMwoxVuNIaNY08W2oR7Ih6lP2");
                                        }
                                    }
                                }
                                mDatabase.child("Users").child(uidDelete).removeValue();

                                // Delete from Firebase Authentication
                                deleteFirebaseAuth(email, password);
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

        final UserAdapter adapter = new UserAdapter(this, R.layout.admin_user_row, userArray);
        final ListView listView = findViewById(R.id.admin_listView_delUser);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArray.clear();
                for (DataSnapshot kv : dataSnapshot.getChildren()) {
                    String email = kv.child("Email").getValue(String.class);
                    String name = kv.child("Name").getValue(String.class);
                    String password = kv.child("Password").getValue(String.class);
                    String uidDelete = kv.getKey();
                    userArray.add(new User(email, name, password, uidDelete));
                }
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


    }

    public void deleteFirebaseAuth(final String email, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                try {
                    String API_KEY = "AIzaSyC3-GY_gyLD2S1P794QXNT7pO7YkrIZw8Y";
                    jPayload.put("email", email);
                    jPayload.put("password", password);
                    jPayload.put("returnSecureToken", true);

                    URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY);
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
                    String idToken = myJsonObject.getString("idToken");

                    if (conn.getResponseCode() == 200) {
                        deleteFirebaseAuthHelper(email, idToken);
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Deleting " + email + " successfully", Snackbar.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Deleting " + email + " successfully", Snackbar.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    public void deleteFirebaseAuthHelper(final String email, final String idToken) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                try {
                    String API_KEY = "AIzaSyC3-GY_gyLD2S1P794QXNT7pO7YkrIZw8Y";
                    jPayload.put("idToken", idToken);

                    URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:delete?key=" + API_KEY);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(jPayload.toString().getBytes());
                    outputStream.close();

                    if (conn.getResponseCode() == 200) {
                        Snackbar.make(findViewById(android.R.id.content), "Deleting " + email + " successfully", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Deleting failed", Snackbar.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Deleting failed", Snackbar.LENGTH_SHORT).show();
                }
            }
        }).start();
    }
}