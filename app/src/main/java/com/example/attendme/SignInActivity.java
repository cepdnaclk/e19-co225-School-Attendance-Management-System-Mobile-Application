package com.example.attendme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signup_email,signup_password;
    private Button sign_in_button;
    private TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        signup_email = findViewById(R.id.signup_email);
        signup_password = findViewById(R.id.signup_password);
        sign_in_button = findViewById(R.id.sign_in_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = signup_email.getText().toString().trim();
                String pass = signup_password.getText().toString().trim();

                if (user.isEmpty()){
                    signup_email.setError("Email cannot be empty. Please Enter a valid email.");

                }
                if (pass.isEmpty()){
                    signup_password.setError("Password cannot be empty");
                }
                else {
                    
                }
            }
        });

    }
}