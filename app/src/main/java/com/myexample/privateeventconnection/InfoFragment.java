package com.myexample.privateeventconnection;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InfoFragment extends Fragment {

    private InfoViewModel mViewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView emailTextView, currentNameTextView;
    private EditText newNameEditText, newPasswordEditText, oldPasswordEditText;
    private Button updateName, updatePassword, adminButton;
    private String uid;
    private FirebaseUser currentUser;

    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.info_fragment, container, false);

        emailTextView = view.findViewById(R.id.yf_textView3);
        newNameEditText = view.findViewById(R.id.yf_editText);
        newPasswordEditText = view.findViewById(R.id.yf_editText2);
        currentNameTextView = view.findViewById(R.id.yf_textView7);
        oldPasswordEditText = view.findViewById(R.id.yf_editText3);
        updateName = view.findViewById(R.id.yf_button);
        updatePassword = view.findViewById(R.id.yf_button2);
        adminButton = view.findViewById(R.id.yf_button3);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Show admin activity if the current user is an admin
        mDatabase.child("Users").child(uid).child("Admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue(Double.class) != 0) {
                    adminButton.setEnabled(true);
                } else {
                    adminButton.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AdminActivity.class);
                startActivity(intent);
            }
        });

        // Show email
        if (currentUser != null) {
            emailTextView.setText("Email: " + currentUser.getEmail());
        }

        // Show name
        mDatabase.child("Users").child(uid).child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentNameTextView.setText("Current Nick Name: " + dataSnapshot.getValue(String.class));
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
                    updateName.setText("Update Nick Name");
                    updateName.setEnabled(true);
                    Toast.makeText(getContext(), "New nick name must be at least one character with no Leading and trailing white spaces.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mDatabase.child("Users").child(uid).child("Name").setValue(newName);
                Snackbar.make(view, "Updating finished", Snackbar.LENGTH_SHORT).show();
                updateName.setText("Update Nick Name");
                newNameEditText.setText("");
                updateName.setEnabled(true);
            }
        });


        // Update password
        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword.setEnabled(false);
                updatePassword.setText("Updating...");
                final String newPassword = newPasswordEditText.getText().toString();
                if (newPassword.length() < 6 || !newPassword.replaceAll("\\s", "").equals(newPassword)) {
                    updatePassword.setText("Update Password");
                    updatePassword.setEnabled(true);
                    Toast.makeText(getContext(), "New password must be at least six characters with no white spaces.",
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
                                                        Snackbar.make(view, "Password updating successful", Snackbar.LENGTH_SHORT).show();
                                                        mDatabase.child("Users").child(uid).child("Password").setValue(newPassword);
                                                    } else {
                                                        Snackbar.make(view, "Password updating failed", Snackbar.LENGTH_SHORT).show();
                                                    }
                                                    newPasswordEditText.setText("");
                                                    oldPasswordEditText.setText("");
                                                    updatePassword.setText("Update Password");
                                                    updatePassword.setEnabled(true);
                                                }
                                            });
                                } else {
                                    Snackbar.make(view, "Authentication failed", Snackbar.LENGTH_SHORT).show();
                                    updatePassword.setEnabled(true);
                                }
                            }
                        });
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(InfoViewModel.class);
        // TODO: Use the ViewModel
    }

}