package com.example.attendme;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

public class ParentNotesActivity extends AppCompatActivity {

    private ArrayList<String> gradeList;
    private String userId, teacherName, teacherGrade;
    private TextView nameLabel, gradeLabel, dateLabel,teacherNameLabel;
    private EditText textReason;
    private ArrayList<String > presentStudents;
    private ArrayList<String > allStudents;
    private FirebaseDatabase database;

    private  DatabaseReference myRef;
    private HashMap<String, Object> attendanceData;
    private String currentDate,studentID;

    public void submit(View view){
        textReason = findViewById(R.id.editTextTextMultiLine);
        String reason = textReason.getText().toString();
        System.out.println(textReason.getText().toString());
        myRef = database.getReference("students");
        myRef.orderByChild("studentID").equalTo(studentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    DatabaseReference attendanceRef = studentSnapshot.getRef().child("attendance");
                    attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot attendanceSnapshot : dataSnapshot.getChildren()) {
                                String attendanceKey = attendanceSnapshot.child(currentDate).getKey();
                                System.out.println(attendanceKey);
                                if (attendanceKey.equals(currentDate)) {
                                    DatabaseReference reasonRef = attendanceSnapshot.getRef().child("reason");
                                    reasonRef.setValue(reason);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_notes);
        currentDate = getCurrentDate();

        database = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        allStudents = new ArrayList<>();
        attendanceData = new HashMap<>();

        userId = null;
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            System.out.println(userId);
        }

        DatabaseReference userRef = database.getReference("users");
        Query query = userRef.orderByChild("userID").equalTo(userId);

        nameLabel = findViewById(R.id.labelStdName);
        gradeLabel = findViewById(R.id.labelGrade);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild("studentID")) {
                            studentID = snapshot.child("studentID").getValue(String.class);
                            searchStudent(studentID);
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
    }

    private void searchStudent(String studentID) {
        System.out.println("Ahhhhhh");
        System.out.println(studentID);
        myRef = database.getReference("students");
        Query query = myRef.orderByChild("studentID").equalTo(studentID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild("name") && snapshot.hasChild("grade")) {
                            String name = snapshot.child("name").getValue(String.class);
                            String grade = snapshot.child("grade").getValue(String.class);
                            nameLabel.setText("Name: " +name);
                            gradeLabel.setText("Grade: " + grade);
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

    }
}