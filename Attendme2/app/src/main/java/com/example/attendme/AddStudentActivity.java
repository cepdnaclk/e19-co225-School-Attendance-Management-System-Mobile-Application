package com.example.attendme;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private EditText editTextName, editTextAddress, editTextFatherName, editTextFatherMobile, editTextFatherOccupation;
    private EditText editTextFatherEmail,editTextMotherEmail, editTextMotherName, editTextMotherMobile, editTextMotherOccupation;
    private EditText editTextGuardianName, editTextGuardianMobile, editTextGuardianOccupation, editTextGuardianAddress;
    private EditText editTextLandPhone, editTextWeight, editTextHeight;

    private Spinner spinnerEmail;

    private Button buttonAddImages, buttonSave;

    private DatabaseReference studentRef,userRef;
    private StorageReference storageReference;

    private Uri imageUri;
    private FirebaseDatabase database;

    private HashMap<String,String> userList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        database = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");
        studentRef = database.getReference().child("students");
        userRef = database.getReference().child("users");
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextFatherName = findViewById(R.id.editTextFatherName);
        editTextFatherMobile = findViewById(R.id.editTextFatherMobile);
        editTextFatherOccupation = findViewById(R.id.editTextFatherOccupation);
        editTextFatherEmail = findViewById(R.id.editTextFatherEmail);
        editTextMotherEmail = findViewById(R.id.editTextMotherEmail);
        editTextMotherName = findViewById(R.id.editTextMotherName);
        editTextMotherMobile = findViewById(R.id.editTextMotherMobile);
        editTextMotherOccupation = findViewById(R.id.editTextMotherOccupation);
        editTextGuardianName = findViewById(R.id.editTextGuardianName);
        editTextGuardianAddress = findViewById(R.id.editTextGuardianAddress);
        editTextGuardianMobile = findViewById(R.id.editTextGuardianMobile);
        editTextGuardianOccupation = findViewById(R.id.editTextGuardianOccupation);
        editTextLandPhone = findViewById(R.id.editTextLandPhone);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextHeight = findViewById(R.id.editTextHeight);
        spinnerEmail = findViewById(R.id.spinner_email);

        buttonAddImages = findViewById(R.id.buttonAddImages);
        buttonSave = findViewById(R.id.buttonSave);

        userList = new HashMap<>();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                spinnerEmail = findViewById(R.id.spinner_email);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    String userID = snapshot.child("userID").getValue(String.class);
                    userList.put(email,userID);
                }
                ArrayList<String> emailList = new ArrayList<>(userList.keySet());
                // Update the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddStudentActivity.this,android.R.layout.simple_spinner_item, emailList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEmail.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        buttonAddImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStudent();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }


    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageUri = getImageUri(imageBitmap);
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Image", null);
        return Uri.parse(path);
    }

    private void saveStudent() {
        // Get the entered student information
        String stdName = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String fatherName = editTextFatherName.getText().toString().trim();
        String fatherMobile = editTextFatherMobile.getText().toString().trim();
        String fatherOccupation = editTextFatherOccupation.getText().toString().trim();
        String fatherEmail = editTextFatherEmail.getText().toString().trim();
        String motherName = editTextMotherName.getText().toString().trim();
        String motherMobile = editTextMotherMobile.getText().toString().trim();
        String motherOccupation = editTextMotherOccupation.getText().toString().trim();
        String motherEmail = editTextMotherEmail.getText().toString().trim();
        String guardianName = editTextGuardianName.getText().toString().trim();
        String guardianAddress = editTextGuardianAddress.getText().toString().trim();
        String guardianMobile = editTextGuardianMobile.getText().toString().trim();
        String guardianOccupation = editTextGuardianOccupation.getText().toString().trim();
        String landPhone = editTextLandPhone.getText().toString().trim();
        String weight = editTextWeight.getText().toString().trim();
        String height = editTextHeight.getText().toString().trim();
        String email = spinnerEmail.getSelectedItem().toString();
        String userID = userList.get(email);

        Map<String, Object> studentData = new HashMap<>();

        DatabaseReference userRef = database.getReference().child("users").child(userID); // Retrieve the correct user reference


        // Create a unique key for the student in the database
        String studentId = studentRef.push().getKey();

        userRef.child("studentID").setValue(studentId);
        userRef.child("name").setValue(guardianName);
        userRef.child("address").setValue(guardianAddress);
        userRef.child("email").setValue(email);
        userRef.child("mobile").setValue(guardianMobile);
        userRef.child("role").setValue("parent");

        studentData.put("name", stdName);
        studentData.put("address", address);
        studentData.put("fatherName", fatherName);
        studentData.put("fatherMobile", fatherMobile);
        studentData.put("fatherOccupation", fatherOccupation);
        studentData.put("fatherEmail", fatherEmail);
        studentData.put("motherName", motherName);
        studentData.put("motherEmail", motherEmail);
        studentData.put("motherMobile", motherMobile);
        studentData.put("motherOccupation", motherOccupation);
        studentData.put("guardianName", guardianName);
        studentData.put("guardianAddress", guardianAddress);
        studentData.put("guardianEmail", email);
        studentData.put("guardianMobile", guardianMobile);
        studentData.put("guardianOccupation", guardianOccupation);
        studentData.put("guardianID", userID);
        studentData.put("landPhone", landPhone);
        studentData.put("weight", weight);
        studentData.put("height", height);
        studentData.put("studentID", studentId);
        studentData.put("currentStudent", true);
        studentData.put("assigned", false);

        if (imageUri != null) {
            // Upload the image to Firebase Storage and get the download URL
            uploadImage(studentId);
            studentData.put("imageUri", imageUri.toString());
        }
        // Save the student data to the database
        studentRef.child(studentId).setValue(studentData)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddStudentActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                        clearForm();
                    } else {
                        Toast.makeText(AddStudentActivity.this, "Failed to add student", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }




    private void uploadImage(String studentId) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + studentId + ".jpg");

        UploadTask uploadTask = storageReference.putFile(imageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image upload successful
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Image upload failed
            }
        });
    }

    private void clearForm() {
        editTextName.setText("");
        editTextAddress.setText("");
        editTextFatherName.setText("");
        editTextFatherMobile.setText("");
        editTextFatherOccupation.setText("");
        editTextFatherEmail.setText("");
        editTextMotherName.setText("");
        editTextMotherMobile.setText("");
        editTextMotherOccupation.setText("");
        editTextMotherEmail.setText("");
        editTextGuardianName.setText("");
        editTextGuardianAddress.setText("");
        editTextGuardianOccupation.setText("");
        editTextGuardianMobile.setText("");
        editTextLandPhone.setText("");
        editTextWeight.setText("");
        editTextHeight.setText("");
        imageUri = null;
    }
}
