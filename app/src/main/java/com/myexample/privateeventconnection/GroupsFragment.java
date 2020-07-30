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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class GroupsFragment extends Fragment {

    private GroupsViewModel mViewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GridView gridView;
    private ArrayList<String> groupNames;





    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groups_fragment, container, false);


        // Initialize gridview.  In order to use findViewById, we have to put a view in the front
        gridView = view.findViewById(R.id.gridview);
        groupNames = new ArrayList<>();

        final CustomAdapter customAdapter = new CustomAdapter();
        gridView.setAdapter(customAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), GroupInfoActivity.class);
                // We need to pass the groupname to GroupInfoActivity,
                // so that GroupInfoActivity can display its corresponding information.
                intent.putExtra("groupname",groupNames.get(position));
                startActivity(intent);
            }
        });



        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String uid = currentUser.getUid();


        // Retrieve an instance of database using reference the location
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Groups");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through all groups and get their names. Add names to groupNames arraylist.
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    groupNames.add(child.getKey());
                }

                // Different thread here. We need to notify the gridview thread to display group information
                // This code is a must.
                customAdapter.notifyDataSetChanged();



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });






        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(GroupsViewModel.class);
        // TODO: Use the ViewModel

    }

    private class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return groupNames.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view1 = getLayoutInflater().inflate(R.layout.row_data,null);
            // Getting view in row_data xml
            TextView name = view1.findViewById(R.id.groupname);
            // Put group name in corresponding textview
            name.setText(groupNames.get(i));

            return view1;



        }
    }
}