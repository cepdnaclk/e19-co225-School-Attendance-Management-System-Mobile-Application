package com.example.attendme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase database  = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userID = null;
        if (firebaseUser != null) {
            userID = firebaseUser.getUid();
            System.out.println(userID);
        }
        DatabaseReference myRef = database.getReference("users").child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    if(role.equals("teacher")){
                        startTeacherActivity();
                    }else if(role.equals("admin")){
                        startAdminActivity();
                    }else if(role.equals("authorized")){
                        startAuthorizedActivity();
                    }else{
                        startUserActivity();
                    }
                    if (role != null) {
                        Log.d("User Role", role);
                    } else {
                        Log.d("User Role", "Role not found.");
                    }
                } else {
                    Log.d("User Role", "User not found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase Error", databaseError.getMessage());
            }
        });
    }

    private void startUserActivity() {
        Intent intent = new Intent(this, ParentActivity.class);
        startActivity(intent);
    }

    private void startAuthorizedActivity() {
        Intent intent = new Intent(this, AdministrationActivity.class);
        startActivity(intent);
    }

    private void startAdminActivity() {
        Intent intent = new Intent(this, AdminsActivity.class);
        startActivity(intent);

    }

    private void startTeacherActivity() {
        Intent intent = new Intent(this, TeachersActivity.class);
        startActivity(intent);
    }
}