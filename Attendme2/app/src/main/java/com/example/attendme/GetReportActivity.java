package com.example.attendme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetReportActivity extends AppCompatActivity {
    private ArrayList<String> presentList;
    private ArrayList<String> gradeList;
    private ArrayList<String> dateList;
    private ArrayList<String> sectionList;

    private Map<String,Boolean> attendance;
    private FirebaseDatabase database;

    private ArrayList<Student> students;
    private Spinner presentSpinner,gradeSpinner,dateSpinner,sectionSpinner;
    private DatabaseReference gradeRef, studentRef;

    private String presentValue,gradeValue,dateValue,sectionValue;
    public void check(View view){
        presentValue = presentSpinner.getSelectedItem().toString();
        gradeValue = gradeSpinner.getSelectedItem().toString();
        dateValue = dateSpinner.getSelectedItem().toString();
        sectionValue = sectionSpinner.getSelectedItem().toString();
        students = new ArrayList<>();

        studentRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String stdID = dataSnapshot.child("studentID").getValue(String.class);
                String grade = dataSnapshot.child("grade").getValue(String.class);
                String name = dataSnapshot.child("name").getValue(String.class);
                String section = dataSnapshot.child("section").getValue(String.class);
                Boolean approved = dataSnapshot.child("approved").getValue(Boolean.class);
                Boolean currentStd = dataSnapshot.child("currentStudent").getValue(Boolean.class);
                Boolean assigned = dataSnapshot.child("assigned").getValue(Boolean.class);
                if(approved && currentStd && assigned) {
                    Student std = new Student(stdID,grade,name,section);
                    Boolean currentStudentValue = dataSnapshot.child("currentStudent").getValue(Boolean.class);
                    if (currentStudentValue != null && currentStudentValue) {
                        DatabaseReference attendanceRef = dataSnapshot.getRef().child("attendance");
                        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot attendanceSnapshot : dataSnapshot.getChildren()) {
                                    for (DataSnapshot child : attendanceSnapshot.getChildren()) {
                                        if (isDateFormatValid(child.getKey())) {
                                            std.addAttendance(child.getKey(), Boolean.parseBoolean(child.getValue().toString()));
                                        }
                                    }
                                }
                                students.add(std);
                                analyseDate(students);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle error
                            }
                        });
                    }
                }
                //System.out.println(dateList);
                //System.out.println(students);
            }

            public boolean isDateFormatValid(String input) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false); // Disable lenient parsing

                try {
                    dateFormat.parse(input);
                    return true;
                } catch (ParseException e) {
                    return false;
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void analyseDate(ArrayList<Student> students) {
        System.out.println(students);
        ArrayList<String> studentList = new ArrayList<>();
        System.out.println(students);
        ListView listView =findViewById(R.id.listView);
        for(Student std:students){
            std.calculateAttendance();
            System.out.println(std);
            if(std.getReport(presentValue,gradeValue,dateValue,sectionValue)){
                String temp = std.getStdName() + " in " + std.getStudentSection() + " section is " + presentValue + " on " + dateValue + " and has attendance precentage of " + String.format("%.3f",std.getPrecentage()*100) + "%";
                System.out.println(temp);
                studentList.add(temp);
            }
        }
        TextView total,present,absent;
        total = findViewById(R.id.totalCount);
        present = findViewById(R.id.presentCount);
        absent = findViewById(R.id.absentCount);
        int presentCount = 0, absentCount = 0;
        for(Student std:students){
            if(std.getReport("Present",gradeValue,dateValue,sectionValue)){
                presentCount++;
            }
            if(std.getReport("Absent",gradeValue,dateValue,sectionValue)){
                absentCount++;
            }
        }
        total.setText("Total Count: " +(presentCount+absentCount));
        present.setText("Present Count: " + presentCount);
        absent.setText("Absent Count: " + absentCount);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,studentList);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_report);

        presentList = new ArrayList<>();
        gradeList = new ArrayList<>();
        dateList = new ArrayList<>();
        sectionList = new ArrayList<>();


        gradeList.add("Any");
        sectionList.add("Any");

        presentList.add("Present");
        presentList.add("Absent");

        sectionList.add("Bio");
        sectionList.add("Maths");
        sectionList.add("Arts");
        sectionList.add("Commerce");

        database = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");

        presentSpinner = findViewById(R.id.spinner_present);
        gradeSpinner = findViewById(R.id.spinner_grade);
        dateSpinner = findViewById(R.id.spinner_date);
        sectionSpinner = findViewById(R.id.spinner_section);

        gradeRef = database.getReference("Classes");
        studentRef = database.getReference("students");


        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(GetReportActivity.this,android.R.layout.simple_spinner_item, presentList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presentSpinner.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(GetReportActivity.this,android.R.layout.simple_spinner_item, sectionList);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(adapter2);

        gradeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterate through the database snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String grade = snapshot.child("grade").getValue(String.class);
                    gradeList.add(grade);
                }

                // Update the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(GetReportActivity.this,android.R.layout.simple_spinner_item, gradeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                gradeSpinner.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Log.i("TAG", "Failed to read value.", databaseError.toException());
            }
        });

        studentRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Boolean currentStudentValue = dataSnapshot.child("currentStudent").getValue(Boolean.class);
                if (currentStudentValue != null && currentStudentValue) {
                    DatabaseReference attendanceRef = dataSnapshot.getRef().child("attendance");
                    attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot attendanceSnapshot : dataSnapshot.getChildren()) {
                                for (DataSnapshot child : attendanceSnapshot.getChildren()) {
                                    if (isDateFormatValid(child.getKey())) {
                                        dateList.add(child.getKey());
                                    }
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(GetReportActivity.this,android.R.layout.simple_spinner_item, dateList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dateSpinner.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                    // Stop listening to further child events
                    studentRef.removeEventListener(this);
                }
            }

            public boolean isDateFormatValid(String input) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);

                try {
                    dateFormat.parse(input);
                    return true;
                } catch (ParseException e) {
                    return false;
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

            // Other methods from ChildEventListener interface...

        });
    }
}