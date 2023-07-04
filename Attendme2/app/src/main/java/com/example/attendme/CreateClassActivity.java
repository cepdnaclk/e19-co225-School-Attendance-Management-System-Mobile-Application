package com.example.attendme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class CreateClassActivity extends AppCompatActivity {
    private EditText gradeName;
    private Button saveBtn;
    private DatabaseReference gradeRef;
    private FirebaseDatabase database;

    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);
        saveBtn = findViewById(R.id.buttonSave);
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGrade();
            }
        });
    }

    private void saveGrade() {
        gradeName = findViewById(R.id.editTextGradeName);
        String grade = gradeName.getText().toString();
        gradeRef = database.getReference().child("Classes").push();
        String recordId = gradeRef.getKey(); // Get the unique key for the new record

        Map<String, Object> teacherData = new HashMap<>();
        teacherData.put("grade", grade);

        gradeRef.setValue(teacherData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Record added successfully
                        Log.d("Firebase", "Record added successfully!");
                        Toast.makeText(CreateClassActivity.this, "Teacher is added successfully", Toast.LENGTH_SHORT).show();
                        clearForm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error adding the record
                        Log.e("Firebase", "Error adding record: " + e.getMessage());
                        Toast.makeText(CreateClassActivity.this, "Adding class failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearForm() {
        gradeName.setText("");
    }
}
