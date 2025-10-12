package com.example.track;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Switch to login screen when "Already have an account" is tapped
        TextView loginLink = findViewById(R.id.alreadyHaveAccount);
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        // Sign-up button (we’ll connect Firebase later)
        Button signUpButton = findViewById(R.id.btn_sign_up);
        signUpButton.setOnClickListener(v -> {
            // TODO: Add Firebase sign-up logic here
        });

        TextView loginTab = findViewById(R.id.tab_signin);
        loginTab.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

    }
}
