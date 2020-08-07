package com.myexample.privateeventconnection;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class InfoFragment extends Fragment {

    private InfoViewModel mViewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView currentNameTextView;
    private LinearLayout adminContainer, updateName, updatePassword, createUser, deleteUser, createGroup, editGroup, uploadAvatar;
    private String uid;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private CircleImageView profileCircleImageView;
    private ImageView imageView_uploading;
    private final long ONE_MEGABYTE = 20 * 1024 * 1024;
    private static final int WRITE_SDCARD_PERMISSION_REQUEST_CODE = 1;
    private static final int CHOICE_FROM_ALBUM_REQUEST_CODE = 4;

    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.info_fragment_design_ui, container, false);
        currentNameTextView = view.findViewById(R.id.yf_textView7_ui);
        updatePassword = view.findViewById(R.id.yf_linearLayout_updatePassword);
        updateName = view.findViewById(R.id.yf_linearLayout_updateName);
        adminContainer = view.findViewById(R.id.info_admin_container);
        createUser = view.findViewById(R.id.yf_linearLayout_createUser);
        deleteUser = view.findViewById(R.id.yf_linearLayout_deleteUser);
        createGroup = view.findViewById(R.id.yf_linearLayout_createGroup);
        editGroup = view.findViewById(R.id.yf_linearLayout_editGroup);
        uploadAvatar = view.findViewById(R.id.yf_linearLayout_uploadAvatar);
        imageView_uploading = view.findViewById(R.id.imageView_uploading);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Show admin activity if the current user is an admin
        mDatabase.child("Users").child(uid).child("Admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue(Double.class) != 0) {
                    adminContainer.setVisibility(View.VISIBLE);
                } else {
                    adminContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Show name
        mDatabase.child("Users").child(uid).child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentNameTextView.setText("Hello " + dataSnapshot.getValue(String.class) + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Show avatar
        // https://firebase.google.com/docs/storage/android/download-files
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("avatars/" + uid + ".jpg");

        profileCircleImageView = view.findViewById(R.id.profileCircleImageView);
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(InfoFragment.this)
                        .load(bytes)
                        .into(profileCircleImageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                profileCircleImageView.setImageResource(R.drawable.default_avatar);
            }
        });

        // Update avatar
        // https://cloud.tencent.com/developer/article/1385038
        uploadAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAvatarHelper();
            }
        });

        // Update name
        updateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UpdateNameActivity.class);
                startActivity(intent);
            }
        });

        // Update password
        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UpdatePasswordActivity.class);
                startActivity(intent);
            }
        });

        // Create user account
        createUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateUserActivity.class);
                startActivity(intent);
            }
        });

        // Delete user account
        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DeleteUserActivity.class);
                startActivity(intent);
            }
        });

        // Create group
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            }
        });

        // Edit group
        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GroupOperationActivity.class);
                startActivity(intent);
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

    public void uploadAvatarHelper() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_SDCARD_PERMISSION_REQUEST_CODE);
        } else {
            uploadAvatarHelperInsider();
        }
    }

    public void uploadAvatarHelperInsider() {
        Intent choiceFromAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        choiceFromAlbumIntent.setType("image/*");
        startActivityForResult(choiceFromAlbumIntent, CHOICE_FROM_ALBUM_REQUEST_CODE);
    }

    // Obtain and extract the returned data from the next activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == CHOICE_FROM_ALBUM_REQUEST_CODE) &&
                (resultCode == RESULT_OK)) {
            // Uploading animation
            Glide.with(InfoFragment.this)
                    .asGif()
                    .load(R.drawable.loading)
                    .into(imageView_uploading);

            Uri file = data.getData();
            UploadTask uploadTask = storageReference.putFile(file);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Snackbar.make(getView(), "Upload failed", Snackbar.LENGTH_SHORT).show();
                    imageView_uploading.setImageResource(R.drawable.baseline_arrow_forward_24);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Snackbar.make(getView(), "Upload successfully", Snackbar.LENGTH_SHORT).show();

                    // Reload the new avatar
                    storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Glide.with(InfoFragment.this)
                                    .load(bytes)
                                    .into(profileCircleImageView);
                            imageView_uploading.setImageResource(R.drawable.baseline_arrow_forward_24);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            imageView_uploading.setImageResource(R.drawable.baseline_arrow_forward_24);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_SDCARD_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadAvatarHelperInsider();
                } else {
                    Toast.makeText(getContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
