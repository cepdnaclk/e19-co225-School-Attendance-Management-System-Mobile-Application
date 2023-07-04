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

public class RemoveStudentActivityTeacher extends AppCompatActivity {
    private CustomAdapter adapter;
    private ArrayList<Student> studentToDisplay;
    private ListView listView;
    private DatabaseReference reference;
    private ArrayList<String> checkedIDs;
    private ArrayList<String> gradeList;

    public void remove(View view){
        checkedIDs = adapter.getCheckedIDs();
        DatabaseReference studentsRef = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("students");

        for (String stdID : checkedIDs) {
            Query query = studentsRef.orderByChild("studentID").equalTo(stdID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference studentRef = childSnapshot.getRef();
                        studentRef.child("currentStudent").setValue(true);
                        studentRef.child("assigned").setValue(false);
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_studnet);


        FirebaseDatabase database = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("students");

        DatabaseReference gradeRef = database.getReference("Classes");
        gradeList = new ArrayList<>();
        studentToDisplay = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);


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

                // Update the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(RemoveStudentActivityTeacher.this, android.R.layout.simple_spinner_item, gradeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Log.i("TAG", "Failed to read value.", databaseError.toException());
            }
        });
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
                            Boolean approved = child.child(("approved")).getValue(Boolean.class);
                            Boolean currentStd = child.child(("currentStudent")).getValue(Boolean.class);
                            if(approved && currentStd && assigned != null && assigned == true && grade.equals(spinner.getSelectedItem().toString())){
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