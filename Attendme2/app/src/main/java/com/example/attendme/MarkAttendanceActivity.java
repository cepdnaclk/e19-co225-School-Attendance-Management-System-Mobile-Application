package com.example.attendme;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.StartupTime;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class MarkAttendanceActivity extends AppCompatActivity {

    private CustomAdapter adapter;
    private ArrayList<Student> studentToDisplay = new ArrayList<>();
    private ListView listView;
    private ArrayList<String> studentList;
    private ArrayList<String> gradeList;
    private String userId, teacherName, teacherGrade;
    private TextView nameLabel, gradeLabel;
    private ArrayList<String > presentStudents;
    private ArrayList<String > allStudents;
    private FirebaseDatabase database;

    private  DatabaseReference myRef;
    private HashMap<String, Object> attendanceData;
    private String currentDate;

    public void submit(View view){
        presentStudents = adapter.getCheckedIDs();
        for(String stdID:allStudents){
            boolean isPresent = false;
            for(String presentStdID:presentStudents){
                if(presentStdID.equals(stdID)){
                    isPresent = true;
                    break;
                }
            }
            attendanceData.put(stdID,isPresent);
        }
        for (Map.Entry<String, Object> entry : attendanceData.entrySet()) {
            String stdId = entry.getKey();
            Boolean isPresent = Boolean.parseBoolean(entry.getValue().toString());
            markAttendance(stdId,isPresent);
        }
    }

    private void markAttendance(String studentId, boolean isPresent) {
        myRef.orderByChild("studentID").equalTo(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String studentKey = childSnapshot.getKey();

                    // Get a reference to the student's "attendance" field
                    DatabaseReference attendanceRef = myRef.child(studentKey).child("attendance");

                    // Generate a unique ID for the attendance record
                    String attendanceID = attendanceRef.push().getKey();

                    // Create a HashMap to hold the attendance data
                    HashMap<String, Object> attendanceData = new HashMap<>();
                    attendanceData.put(currentDate, isPresent);

                    // Set the attendance record within the "attendance" field using the unique ID
                    attendanceRef.child(attendanceID).setValue(attendanceData);

                    // Check if the student is absent and send a notification to the guardian
                    if (!isPresent) {
                        String guardianId = childSnapshot.child("guardianID").getValue(String.class);
                        sendNotificationToGuardian(guardianId);
                    }

                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur
            }
        });
    }

    private void sendNotificationToGuardian(String guardianId) {
        DatabaseReference usersRef = database.getReference("users");
        Query query = usersRef.orderByChild("userID").equalTo(guardianId);
        System.out.println("----------" + guardianId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild("token")) {
                            String deviceToken = snapshot.child("token").getValue(String.class);
                            System.out.println("Tokeeeen " + deviceToken);
                            sendNotification(deviceToken);
                        } else {
                            Log.d(TAG, "Missing deviceToken field for the guardian");
                        }
                    }
                } else {
                    Log.d(TAG, "Guardian not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur
            }
        });
    }

    public void sendNotification(String recipientToken) {
        RemoteMessage.Builder builder = new RemoteMessage.Builder("campaign")
                .setMessageId(Integer.toString(0))
                .addData("title", "New Campaign")
                .addData("body", "Check out our latest offers!");

        // Send the message
        FirebaseMessaging.getInstance().send(builder.build());
        try {
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer AAAA97ptIsU:APA91bEaGA9XnhdQKRw9vewez_W_0UMg9Oc3zPzsIFjwxHKNVkP98Gro_E1GXfMV7XgJfTvuV49WltkltdSNRJjW2qcDQn-8A5XXjrMohsUcyQuRxYuzNybwb58wsAWSnSED4lFtRngE");
            conn.setDoOutput(true);

            JSONObject notification = new JSONObject();
            notification.put("title", "New message");
            notification.put("body", "You have a new message");

            JSONObject data = new JSONObject();
            data.put("key1", "value1");
            data.put("key2", "value2");

            JSONObject payload = new JSONObject();
            payload.put("notification", notification);
            payload.put("data", data);
            payload.put("to", recipientToken);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(payload.toString().getBytes("UTF-8"));
            outputStream.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Notification sent successfully
            } else {
                // Handle error
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int generateMessageId() {
        // Generate a unique message ID using a suitable logic
        // You can use a timestamp, random number, or any other approach that suits your requirements
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }



    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
        currentDate = getCurrentDate();

        database  = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        myRef = database.getReference("students");

        allStudents = new ArrayList<>();
        attendanceData = new HashMap<>();

        userId = null;
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            System.out.println(userId);
        }
        DatabaseReference userRef = database.getReference("users");
        Query query = userRef.orderByChild("userID").equalTo(userId);

        nameLabel = findViewById(R.id.label_teacher);
        gradeLabel = findViewById(R.id.label_grade);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild("name") && snapshot.hasChild("grade")) {
                            teacherName = snapshot.child("name").getValue(String.class);
                            teacherGrade = snapshot.child("grade").getValue(String.class);
                            nameLabel.setText("Teacher : "+ teacherName);
                            gradeLabel.setText("Grade : " +teacherGrade);
                        } else {
                            Log.d("Firebase", "Missing name or grade fields for the user");
                        }
                    }
                } else {
                    Log.d("Firebase", "User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        listView = (ListView) findViewById(R.id.listView);
        gradeList = new ArrayList<>();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                studentToDisplay.clear();
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    String stdName = child.child("name").getValue(String.class);
                    String stdID = child.child("studentID").getValue(String.class);
                    String grade = child.child("grade").getValue(String.class);
                    if(grade.equals(teacherGrade)){
                        studentToDisplay.add(new Student(stdName,stdID));
                        allStudents.add(stdID);
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