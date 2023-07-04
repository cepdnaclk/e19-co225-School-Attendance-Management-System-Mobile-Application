package com.example.attendme;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddTeacherActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress,editTextMobile,editTextEmail;
    private Button buttonSave;
    private DatabaseReference databaseReference;
    DatabaseReference userRef;
    private StorageReference storageReference;
    private ArrayList<String> gradeList;
    private HashMap<String,String> userList;
    private Spinner spinnerGrade,spinnerEmail;
    private FirebaseDatabase database;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);

        gradeList = new ArrayList<>();
        userList = new HashMap<>();

        // Initialize Firebase database and storage references
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextMobile = findViewById(R.id.editTextMobile);
        buttonSave = findViewById(R.id.buttonSave);


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTeacher();
            }
        });

        DatabaseReference gradeRef = database.getReference("Classes");
        DatabaseReference usersRef = database.getReference("users");

        gradeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gradeList.clear();
                spinnerGrade = findViewById(R.id.spinner_grade);
                // Iterate through the database snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String grade = snapshot.child("grade").getValue(String.class);
                    gradeList.add(grade);
                }

                // Update the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTeacherActivity.this,android.R.layout.simple_spinner_item, gradeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGrade.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                spinnerEmail = findViewById(R.id.spinner_email);
                // Iterate through the database snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    String userID = snapshot.child("userID").getValue(String.class);
                    userList.put(email,userID);
                }
                ArrayList<String> emailList = new ArrayList<>(userList.keySet());
                // Update the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTeacherActivity.this,android.R.layout.simple_spinner_item, emailList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEmail.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void saveTeacher() {
        // Get teacher information
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String mobile = editTextMobile.getText().toString().trim();
        String grade = spinnerGrade.getSelectedItem().toString();
        String email = spinnerEmail.getSelectedItem().toString();
        String userID = userList.get(email);

        userRef = database.getReference().child("users").child(userID);

        Map<String, Object> teacherData = new HashMap<>();
        teacherData.put("name", name);
        teacherData.put("address", address);
        teacherData.put("mobile", mobile);
        teacherData.put("grade", grade);
        teacherData.put("role","teacher");

        userRef.updateChildren(teacherData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Fields updated successfully
                        Log.d("Firebase", "Fields updated successfully!");
                        Toast.makeText(AddTeacherActivity.this, "Teacher is added successfully", Toast.LENGTH_SHORT).show();
                        clearForm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error updating fields
                        Log.e("Firebase", "Error updating fields: " + e.getMessage());
                        Toast.makeText(AddTeacherActivity.this, "Adding teacher failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearForm() {
        editTextName.setText("");
        editTextAddress.setText("");
        editTextMobile.setText("");
    }
}