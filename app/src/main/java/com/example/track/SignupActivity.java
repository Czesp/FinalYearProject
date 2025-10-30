package com.example.track;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.track.Model.userModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import io.github.rupinderjeet.kprogresshud.KProgressHUD;

public class SignupActivity extends AppCompatActivity {

    private EditText edName, edEmail, edPhone, edPass;

    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore firestore;

    String UserId;
    KProgressHUD ProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        edName = findViewById(R.id.inputName);
        edEmail = findViewById(R.id.inputEmail);
        edPhone = findViewById(R.id.inputPhone);
        edPass = findViewById(R.id.inputPassword);


        // Switch to login screen when "Already have an account" is tapped
        TextView loginLink = findViewById(R.id.alreadyHaveAccount);
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        // Sign-up button (we’ll connect Firebase later)
        AppCompatButton signUpButton = findViewById(R.id.btn_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        signUpButton.setOnClickListener(v -> {
            SignUpUser();
        });

        TextView signinTab = findViewById(R.id.tab_signin);
        signinTab.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        View underline = findViewById(R.id.underline);
        ObjectAnimator.ofFloat(underline, "translationX", 250f).setDuration(300).start();

    }

    private void ProgressBar() {
        ProgressHUD = KProgressHUD.create(SignupActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setMaxProgress(100)
                .setBackgroundColor(R.color.primary_blue)
                .show();
        ProgressHUD.setProgress(90);
    }

//    private void SignUpUser() {
//        String name = edName.getText().toString().trim();
//        String email = edEmail.getText().toString().trim();
//        String phone = edPhone.getText().toString().trim();
//        String pass = edPass.getText().toString().trim();
//
//        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
//                || TextUtils.isEmpty(pass)){
//            Toast.makeText(SignupActivity.this,"Please fill all fields",Toast.LENGTH_LONG).show();
//            return;
//        }else {
//            ProgressBar();
//            firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if(task.isSuccessful()){
//                        if(firebaseAuth.getCurrentUser()!=null){
//                            UserId=firebaseAuth.getCurrentUser().getUid();
//                        } else{
//                            Toast.makeText(SignupActivity.this,
//                                    "Signup failed: " + task.getException().getMessage(),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        DocumentReference UserInfo=firestore.collection("Users").document(UserId);
//                        userModel model = new userModel(name,email,phone,UserId);
//                        UserInfo.set(model, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void unused) {
//                                kProgressHUD.dismiss();
//                                Toast.makeText(SignupActivity.this,"User registered successfully",Toast.LENGTH_SHORT).show();
//                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
//                                startActivity(intent);
//                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//                                finish();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//
//                                kProgressHUD.dismiss();
//                                Toast.makeText(SignupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    kProgressHUD.dismiss();
//                    Toast.makeText(SignupActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
    private void SignUpUser() {
        String name = edName.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String pass = edPass.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(pass)) {
            Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        ProgressBar();

        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (firebaseAuth.getCurrentUser() != null) {
                            UserId = firebaseAuth.getCurrentUser().getUid();
                        } else {
                            if (ProgressHUD != null && ProgressHUD.isShowing()) ProgressHUD.dismiss();
                            Toast.makeText(SignupActivity.this, "Signup failed: no user returned", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentReference userInfo = firestore.collection("Users").document(UserId);
                        userModel model = new userModel(name, email, phone, UserId);

                        userInfo.set(model, SetOptions.merge())
                                .addOnSuccessListener(unused -> {
                                    if (ProgressHUD != null && ProgressHUD.isShowing()) ProgressHUD.dismiss();
                                    Toast.makeText(SignupActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    if (ProgressHUD != null && ProgressHUD.isShowing()) ProgressHUD.dismiss();
                                    Toast.makeText(SignupActivity.this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        if (ProgressHUD != null && ProgressHUD.isShowing()) ProgressHUD.dismiss();
                        Toast.makeText(SignupActivity.this, "Auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (ProgressHUD != null && ProgressHUD.isShowing()) ProgressHUD.dismiss();
                    Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}
