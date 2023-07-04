package com.example.attendme;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EnrolStudentActivity extends AppCompatActivity {

    CustomAdapter adapter;
    ArrayList<Student> studentToDisplay = new ArrayList<>();
    ListView listView;
    DatabaseReference reference;
    ArrayList<String> checkedIDs;
    ArrayList<String> gradeList;


    public void add(View view){
        checkedIDs = adapter.getCheckedIDs();
        Spinner spinner = findViewById(R.id.spinner_grade);
        String selectedGrade = spinner.getSelectedItem().toString();

        DatabaseReference studentsRef = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("students");

        for (String stdID : checkedIDs) {
            Query query = studentsRef.orderByChild("studentID").equalTo(stdID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference studentRef = childSnapshot.getRef();
                        studentRef.child("grade").setValue(selectedGrade);
                        studentRef.child("assigned").setValue(true);
                        studentRef.child("approved").setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to update grade: " + databaseError.getMessage());
                }
            });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("students");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_student);

        listView = (ListView) findViewById(R.id.listView);
        gradeList = new ArrayList<>();

        Spinner spinner = findViewById(R.id.spinner_grade);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String grade = (String) parent.getItemAtPosition(position);
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        studentToDisplay.clear();
                        for(DataSnapshot child: dataSnapshot.getChildren()){
                            String stdName = child.child("name").getValue(String.class);
                            Boolean assigned = child.child("assigned").getValue(Boolean.class);
                            String stdID = child.child("studentID").getValue(String.class);
                            String grade = child.child("grade").getValue(String.class);

                            if(assigned != null && assigned != true && grade.equals(spinner.getSelectedItem().toString())){
                                studentToDisplay.add(new Student(stdName,assigned,stdID));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        DatabaseReference gradeRef = database.getReference("Classes");

        gradeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gradeList.clear();
                Spinner spinner = findViewById(R.id.spinner_grade);
                // Iterate through the database snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String grade = snapshot.child("grade").getValue(String.class);
                    gradeList.add(grade);
                }

                // Update the spinner adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(EnrolStudentActivity.this,
                        android.R.layout.simple_spinner_item, gradeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        adapter = new CustomAdapter(studentToDisplay, getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Student Student= (Student) studentToDisplay.get(position);
                Student.setAssigned(!Student.getAssigned());
                adapter.notifyDataSetChanged();
            }
        });



    }
}