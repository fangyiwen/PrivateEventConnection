package com.myexample.privateeventconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AfterLoginActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    NavController navController;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Context myContext;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        myContext = AfterLoginActivity.this;

        // Start service for checking account existence
        intent = new Intent(myContext, MyService.class);
        startService(intent);

        // Build bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_nav);
        navController = Navigation.findNavController(this, R.id.fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(bottomNavigationView.getMenu()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);


        mAuth = FirebaseAuth.getInstance();

        // Initialize database for a newly created user
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        final String uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mDatabase.child("Users").child(uid).child("Admin").setValue(0);
                    mDatabase.child("Users").child(uid).child("Groups").child("DefaultGroup").setValue(false);
                    mDatabase.child("Users").child(uid).child("Email").setValue(currentUser.getEmail());
                    mDatabase.child("Users").child(uid).child("Name").setValue("User (" + (currentUser.getEmail()) + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }

    // Build top right menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                // Sign out
                mAuth.signOut();
                // Go back to LoginActivity
                Intent intent = new Intent(AfterLoginActivity.this, LoginActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}