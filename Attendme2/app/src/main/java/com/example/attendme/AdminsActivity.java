package com.example.attendme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admins);

        CardView createClass = findViewById(R.id.create_class_card);
        CardView removeStudent = findViewById(R.id.remove_std);
        CardView addTeachers = findViewById(R.id.addTeachers);
        CardView addStudents = findViewById(R.id.addStudents);
        CardView approveAddStudents = findViewById(R.id.approveAddStd);
        CardView approveRemoveStudents = findViewById(R.id.approveRemoveStd);

        addTeachers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddTeacherActivity.class);
                v.getContext().startActivity(intent);

            }
        });

        createClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CreateClassActivity.class);
                v.getContext().startActivity(intent);
            }
        });

        removeStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RemoveStudentActivityAdmin.class);
                v.getContext().startActivity(intent);
            }
        });

        addStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddStudentActivity.class);
                v.getContext().startActivity(intent);
            }
        });

        approveAddStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddApproveStudentActivityAdmin.class);
                v.getContext().startActivity(intent);
            }
        });

        approveRemoveStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RemoveApproveStudentActivityAdmin.class);
                v.getContext().startActivity(intent);
            }
        });


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase database  = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");
        String userID = null;
        if (firebaseUser != null) {
            userID = firebaseUser.getUid();
            System.out.println(userID);
        }
        DatabaseReference myRef = database.getReference("users").child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    TextView textView = findViewById(R.id.textView);
                    textView.setText("Welcome! " + name );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase Error", databaseError.getMessage());
            }
        });

    }
}