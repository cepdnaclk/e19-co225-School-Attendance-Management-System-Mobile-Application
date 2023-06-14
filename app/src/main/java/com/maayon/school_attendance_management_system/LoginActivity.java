package com.maayon.school_attendance_management_system;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.util.PrimitiveIterator;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}